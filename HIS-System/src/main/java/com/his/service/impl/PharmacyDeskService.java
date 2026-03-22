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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PharmacyDeskService implements IPharmacyDeskService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PharmacyDeskMapper pharmacyDeskMapper;

    @Autowired
    private DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper;

    @Autowired
    private DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper;

    @Autowired
    private DmsRegistrationMapper dmsRegistrationMapper;

    private Long requireStaffId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private boolean isPaidRegistration(Long registrationId) {
        if (registrationId == null) {
            return false;
        }
        DmsRegistration reg = dmsRegistrationMapper.selectById(registrationId);
        return reg != null && reg.getBindStatus() != null && reg.getBindStatus() == 1;
    }

    private static final int DEFAULT_QUEUE_LIMIT = 80;
    private static final int SEARCH_QUEUE_LIMIT = 300;
    private static final int MAX_QUEUE_LIMIT = 500;

    @Override
    public Result<List<PharmacyPatientQueueVo>> listPatients(String token, String mode, String keyword, Integer limit) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        String m = (mode == null || mode.trim().isEmpty()) ? "pending" : mode.trim();
        if (!"pending".equals(m) && !"dispensed".equals(m)) {
            return Result.error(ResultCode.PARAM_ERROR, "mode 仅支持 pending / dispensed");
        }
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        int lim = resolveQueueLimit(limit, kw);
        List<PharmacyPatientQueueVo> list = pharmacyDeskMapper.selectPatientQueue(m, kw, lim);
        return Result.success("查询成功", list == null ? new ArrayList<>() : list);
    }

    private static int resolveQueueLimit(Integer requested, String keywordTrimmed) {
        if (requested != null && requested > 0) {
            return Math.min(requested, MAX_QUEUE_LIMIT);
        }
        return keywordTrimmed == null ? DEFAULT_QUEUE_LIMIT : SEARCH_QUEUE_LIMIT;
    }

    @Override
    public Result<List<PharmacyMedicineLineVo>> listMedicineLines(String token, Long registrationId) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (registrationId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        if (!isPaidRegistration(registrationId)) {
            return Result.error(ResultCode.PARAM_ERROR, "仅可查看已缴费挂号的处方");
        }
        List<PharmacyMedicineLineVo> list = pharmacyDeskMapper.selectMedicineLines(registrationId);
        return Result.success("查询成功", list == null ? new ArrayList<>() : list);
    }

    @Override
    @Transactional
    public Result<Object> dispenseItems(String token, Map<String, Object> body) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        List<Long> ids = parseIdList(body == null ? null : body.get("itemIds"));
        if (ids.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "请选择要发药的明细");
        }
        List<DmsMedicineItemRecord> rows = dmsMedicineItemRecordMapper.selectByIds(ids);
        if (rows == null || rows.size() != ids.size()) {
            return Result.error(ResultCode.PARAM_ERROR, "存在无效的药品明细");
        }
        for (DmsMedicineItemRecord row : rows) {
            if (row.getStatus() != null && row.getStatus() == 2) {
                return Result.error(ResultCode.PARAM_ERROR, "含已退款明细，无法发药");
            }
            if (row.getCurrentNum() == null || row.getCurrentNum() <= 0) {
                return Result.error(ResultCode.PARAM_ERROR, "含无需发药或已发完的明细");
            }
            DmsMedicinePrescriptionRecord pr = dmsMedicinePrescriptionRecordMapper.selectById(row.getPrescriptionId());
            if (pr == null || pr.getRegistrationId() == null) {
                return Result.error(ResultCode.PARAM_ERROR, "处方数据异常");
            }
            if (!isPaidRegistration(pr.getRegistrationId())) {
                return Result.error(ResultCode.PARAM_ERROR, "挂号未缴费，不能发药");
            }
        }
        int n = dmsMedicineItemRecordMapper.dispenseByIds(ids);
        if (n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "发药失败");
        }
        return Result.success("发药成功");
    }

    @Override
    @Transactional
    public Result<Object> refundItems(String token, Map<String, Object> body) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        List<Long> ids = parseIdList(body == null ? null : body.get("itemIds"));
        if (ids.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "请选择要退药的明细");
        }
        List<DmsMedicineItemRecord> rows = dmsMedicineItemRecordMapper.selectByIds(ids);
        if (rows == null || rows.size() != ids.size()) {
            return Result.error(ResultCode.PARAM_ERROR, "存在无效的药品明细");
        }
        for (DmsMedicineItemRecord row : rows) {
            if (row.getStatus() != null && row.getStatus() == 2) {
                return Result.error(ResultCode.PARAM_ERROR, "含已退药明细");
            }
            DmsMedicinePrescriptionRecord pr = dmsMedicinePrescriptionRecordMapper.selectById(row.getPrescriptionId());
            if (pr == null || pr.getRegistrationId() == null) {
                return Result.error(ResultCode.PARAM_ERROR, "处方数据异常");
            }
            if (!isPaidRegistration(pr.getRegistrationId())) {
                return Result.error(ResultCode.PARAM_ERROR, "挂号未缴费，不能退药");
            }
        }
        int n = dmsMedicineItemRecordMapper.refundLineByIds(ids);
        if (n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "退药失败");
        }
        return Result.success("退药已登记（演示：未联动收银退费）");
    }

    @SuppressWarnings("unchecked")
    private static List<Long> parseIdList(Object raw) {
        List<Long> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        if (raw instanceof List) {
            for (Object o : (List<?>) raw) {
                if (o instanceof Number) {
                    out.add(((Number) o).longValue());
                } else if (o != null) {
                    try {
                        out.add(Long.parseLong(o.toString()));
                    } catch (Exception ignored) {
                        // skip
                    }
                }
            }
        }
        return out;
    }
}
