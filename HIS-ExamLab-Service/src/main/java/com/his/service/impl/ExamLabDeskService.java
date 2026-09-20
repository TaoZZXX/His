package com.his.service.impl;

import com.his.domain.DmsNonDrugItemRecord;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.mapper.BmsPayableItemMapper;
import com.his.mapper.DmsNonDrugItemRecordMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.service.IExamLabDeskService;
import com.his.utils.JwtUtil;
import com.his.vo.ExamLabItemRowVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class ExamLabDeskService implements IExamLabDeskService {
    private final JwtUtil jwtUtil;
    private final DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;
    private final SmsStaffMapper smsStaffMapper;
    private final BmsPayableItemMapper bmsPayableItemMapper;

    public ExamLabDeskService(JwtUtil jwtUtil, DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper, SmsStaffMapper smsStaffMapper, BmsPayableItemMapper bmsPayableItemMapper) {
        this.jwtUtil = jwtUtil;
        this.dmsNonDrugItemRecordMapper = dmsNonDrugItemRecordMapper;
        this.smsStaffMapper = smsStaffMapper;
        this.bmsPayableItemMapper = bmsPayableItemMapper;
    }

    private Long requireStaffId(String token) { return token == null || token.trim().isEmpty() ? null : jwtUtil.getUserIdFromToken(token); }
    private Long resolveStaffDeptId(Long staffId) { Integer d = smsStaffMapper.selectDeptIdById(staffId); return d == null ? null : d.longValue(); }
    private boolean isNonDrugItemPaid(Long itemId) {
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(itemId);
        if (rec == null || rec.getRegistrationId() == null) return false;
        Integer status = bmsPayableItemMapper.selectPayableStatus(rec.getRegistrationId(), 1, itemId);
        return status != null && status == 1;
    }

    @Override
    public Result<List<ExamLabItemRowVo>> listQueue(String token, String mode, boolean filterByStaffDept) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        String m = (mode == null || mode.trim().isEmpty()) ? "execute" : mode.trim();
        if (!"execute".equals(m) && !"result".equals(m)) return Result.error(ResultCode.PARAM_ERROR, "mode 仅支持 execute / result");
        List<ExamLabItemRowVo> list = dmsNonDrugItemRecordMapper.selectExamLabQueue(m, filterByStaffDept ? resolveStaffDeptId(staffId) : null);
        return Result.success("查询成功", list);
    }

    @Override
    public Result<List<ExamLabItemRowVo>> listWorkbench(String token, String keyword, boolean filterByStaffDept, Integer limit) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        String kw = keyword == null ? null : keyword.trim();
        int lim = (limit != null && limit > 0) ? Math.min(limit, 500) : (kw == null || kw.isEmpty() ? 100 : 400);
        return Result.success("查询成功", dmsNonDrugItemRecordMapper.selectMedTechWorkbench((kw == null || kw.isEmpty()) ? null : kw, filterByStaffDept ? resolveStaffDeptId(staffId) : null, lim));
    }

    @Override
    @Transactional
    public Result<Object> executeItem(Long itemId, String token) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (itemId == null) return Result.error(ResultCode.PARAM_ERROR, "项目ID不能为空");
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(itemId);
        if (rec == null) return Result.error(ResultCode.PARAM_ERROR, "记录不存在");
        if (rec.getType() == null || (rec.getType() != 1 && rec.getType() != 3)) return Result.error(ResultCode.PARAM_ERROR, "非检查/检验项目");
        if (rec.getStatus() != null && rec.getStatus() == 2) return Result.error(ResultCode.PARAM_ERROR, "记录已作废");
        if (rec.getStatus() != null && rec.getStatus() == 1) return Result.error(ResultCode.PARAM_ERROR, "已登记，请勿重复操作");
        if (!isNonDrugItemPaid(itemId)) return Result.error(ResultCode.PARAM_ERROR, "患者尚未缴纳该检查/检验费用，请其至收费处缴费后再执行登记");
        Integer n = dmsNonDrugItemRecordMapper.updateExecuteById(itemId, staffId, LocalDateTime.now());
        return (n == null || n <= 0) ? Result.error(ResultCode.SERVER_ERROR, "执行失败") : Result.success("登记成功");
    }

    @Override
    @Transactional
    public Result<Object> saveResult(Long itemId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (itemId == null) return Result.error(ResultCode.PARAM_ERROR, "项目ID不能为空");
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(itemId);
        if (rec == null) return Result.error(ResultCode.PARAM_ERROR, "记录不存在");
        if (rec.getStatus() == null || rec.getStatus() != 1) return Result.error(ResultCode.PARAM_ERROR, "请先完成执行登记后再录入结果");
        String checkResult = body == null ? null : asString(body.get("checkResult"));
        if (checkResult == null || checkResult.trim().isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "检查/检验结果不能为空");
        String resultImgUrlList = body != null && body.containsKey("resultImgUrlList") ? normalizeImgUrlList(body.get("resultImgUrlList")) : rec.getResultImgUrlList();
        Integer n = dmsNonDrugItemRecordMapper.updateResultById(itemId, staffId, LocalDateTime.now(), checkResult.trim(),
                trim(asString(body == null ? null : body.get("clinicalImpression"))),
                trim(asString(body == null ? null : body.get("clinicalDiagnosis"))),
                resultImgUrlList);
        return (n == null || n <= 0) ? Result.error(ResultCode.SERVER_ERROR, "保存结果失败") : Result.success("结果已保存");
    }

    private static String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static String normalizeImgUrlList(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (Collection<?>) raw) { if (o == null) continue; String s = String.valueOf(o).trim(); if (s.isEmpty()) continue; if (sb.length() > 0) sb.append(','); sb.append(s); }
            return sb.length() == 0 ? null : sb.toString();
        }
        String s = String.valueOf(raw).trim();
        return s.isEmpty() ? null : s;
    }
}
