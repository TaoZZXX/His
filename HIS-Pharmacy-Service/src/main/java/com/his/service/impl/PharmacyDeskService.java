package com.his.service.impl;

import com.his.domain.DmsMedicineItemRecord;
import com.his.domain.DmsMedicinePrescriptionRecord;
import com.his.domain.DmsRegistration;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.mapper.DmsMedicineItemRecordMapper;
import com.his.mapper.DmsMedicinePrescriptionRecordMapper;
import com.his.mapper.DmsRegistrationMapper;
import com.his.mapper.PharmacyDeskMapper;
import com.his.service.IPharmacyDeskService;
import com.his.utils.JwtUtil;
import com.his.vo.PharmacyMedicineLineVo;
import com.his.vo.PharmacyPatientQueueVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PharmacyDeskService implements IPharmacyDeskService {
    private final JwtUtil jwtUtil;
    private final PharmacyDeskMapper pharmacyDeskMapper;
    private final DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper;
    private final DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper;
    private final DmsRegistrationMapper dmsRegistrationMapper;

    public PharmacyDeskService(JwtUtil jwtUtil, PharmacyDeskMapper pharmacyDeskMapper, DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper, DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper, DmsRegistrationMapper dmsRegistrationMapper) {
        this.jwtUtil = jwtUtil; this.pharmacyDeskMapper = pharmacyDeskMapper; this.dmsMedicineItemRecordMapper = dmsMedicineItemRecordMapper; this.dmsMedicinePrescriptionRecordMapper = dmsMedicinePrescriptionRecordMapper; this.dmsRegistrationMapper = dmsRegistrationMapper;
    }
    private Long requireStaffId(String token) { return token == null || token.trim().isEmpty() ? null : jwtUtil.getUserIdFromToken(token); }
    private boolean isPaidRegistration(Long registrationId) { DmsRegistration reg = dmsRegistrationMapper.selectById(registrationId); return reg != null && reg.getBindStatus() != null && reg.getBindStatus() == 1; }
    private static int resolveQueueLimit(Integer requested, String keywordTrimmed) { if (requested != null && requested > 0) return Math.min(requested, 500); return keywordTrimmed == null ? 80 : 300; }

    @Override
    public Result<List<PharmacyPatientQueueVo>> listPatients(String token, String mode, String keyword, Integer limit) {
        if (requireStaffId(token) == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        String m = (mode == null || mode.trim().isEmpty()) ? "pending" : mode.trim();
        if (!"pending".equals(m) && !"dispensed".equals(m)) return Result.error(ResultCode.PARAM_ERROR, "mode 仅支持 pending / dispensed");
        String kw = keyword == null ? null : keyword.trim(); if (kw != null && kw.isEmpty()) kw = null;
        List<PharmacyPatientQueueVo> list = pharmacyDeskMapper.selectPatientQueue(m, kw, resolveQueueLimit(limit, kw));
        return Result.success("查询成功", list == null ? new ArrayList<>() : list);
    }

    @Override
    public Result<List<PharmacyMedicineLineVo>> listMedicineLines(String token, Long registrationId) {
        if (requireStaffId(token) == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (registrationId == null) return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        if (!isPaidRegistration(registrationId)) return Result.error(ResultCode.PARAM_ERROR, "仅可查看已缴费挂号的处方");
        List<PharmacyMedicineLineVo> list = pharmacyDeskMapper.selectMedicineLines(registrationId);
        return Result.success("查询成功", list == null ? new ArrayList<>() : list);
    }

    @Override
    @Transactional
    public Result<Object> dispenseItems(String token, Map<String, Object> body) {
        if (requireStaffId(token) == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        List<Long> ids = parseIdList(body == null ? null : body.get("itemIds"));
        if (ids.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "请选择要发药的明细");
        List<DmsMedicineItemRecord> rows = dmsMedicineItemRecordMapper.selectByIds(ids);
        if (rows == null || rows.size() != ids.size()) return Result.error(ResultCode.PARAM_ERROR, "存在无效的药品明细");
        for (DmsMedicineItemRecord row : rows) {
            if (row.getStatus() != null && row.getStatus() == 2) return Result.error(ResultCode.PARAM_ERROR, "含已退款明细，无法发药");
            if (row.getCurrentNum() == null || row.getCurrentNum() <= 0) return Result.error(ResultCode.PARAM_ERROR, "含无需发药或已发完的明细");
            DmsMedicinePrescriptionRecord pr = dmsMedicinePrescriptionRecordMapper.selectById(row.getPrescriptionId());
            if (pr == null || pr.getRegistrationId() == null) return Result.error(ResultCode.PARAM_ERROR, "处方数据异常");
            if (!isPaidRegistration(pr.getRegistrationId())) return Result.error(ResultCode.PARAM_ERROR, "挂号未缴费，不能发药");
        }
        int n = dmsMedicineItemRecordMapper.dispenseByIds(ids);
        return n <= 0 ? Result.error(ResultCode.SERVER_ERROR, "发药失败") : Result.success("发药成功");
    }

    @Override
    @Transactional
    public Result<Object> refundItems(String token, Map<String, Object> body) {
        if (requireStaffId(token) == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        List<Long> ids = parseIdList(body == null ? null : body.get("itemIds"));
        if (ids.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "请选择要退药的明细");
        List<DmsMedicineItemRecord> rows = dmsMedicineItemRecordMapper.selectByIds(ids);
        if (rows == null || rows.size() != ids.size()) return Result.error(ResultCode.PARAM_ERROR, "存在无效的药品明细");
        for (DmsMedicineItemRecord row : rows) {
            if (row.getStatus() != null && row.getStatus() == 2) return Result.error(ResultCode.PARAM_ERROR, "含已退药明细");
            DmsMedicinePrescriptionRecord pr = dmsMedicinePrescriptionRecordMapper.selectById(row.getPrescriptionId());
            if (pr == null || pr.getRegistrationId() == null) return Result.error(ResultCode.PARAM_ERROR, "处方数据异常");
            if (!isPaidRegistration(pr.getRegistrationId())) return Result.error(ResultCode.PARAM_ERROR, "挂号未缴费，不能退药");
        }
        int n = dmsMedicineItemRecordMapper.refundLineByIds(ids);
        return n <= 0 ? Result.error(ResultCode.SERVER_ERROR, "退药失败") : Result.success("退药已登记（演示：未联动收银退费）");
    }

    private static List<Long> parseIdList(Object raw) {
        List<Long> out = new ArrayList<>();
        if (!(raw instanceof List)) return out;
        for (Object o : (List<?>) raw) {
            if (o instanceof Number) out.add(((Number) o).longValue());
            else if (o != null) try { out.add(Long.parseLong(o.toString())); } catch (Exception ignored) {}
        }
        return out;
    }
}
