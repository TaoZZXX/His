package com.his.service.impl;

import com.his.domain.DmsNonDrugItemRecord;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.mapper.DmsNonDrugItemRecordMapper;
import com.his.service.IBmsCashierService;
import com.his.service.IExamLabDeskService;
import com.his.utils.JwtUtil;
import com.his.vo.ExamLabItemRowVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class ExamLabDeskService implements IExamLabDeskService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;

    @Autowired
    private IBmsCashierService bmsCashierService;

    private Long requireStaffId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private Long resolveStaffDeptId(Long staffId) {
        if (staffId == null) {
            return null;
        }
        try {
            Integer deptId = jdbcTemplate.queryForObject(
                    "select dept_id from sms_staff where id = ?",
                    Integer.class,
                    staffId
            );
            return deptId == null ? null : deptId.longValue();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Result<List<ExamLabItemRowVo>> listQueue(String token, String mode, boolean filterByStaffDept) {
        Long staffId = requireStaffId(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        String m = (mode == null || mode.trim().isEmpty()) ? "execute" : mode.trim();
        if (!"execute".equals(m) && !"result".equals(m)) {
            return Result.error(ResultCode.PARAM_ERROR, "mode 仅支持 execute / result");
        }
        Long filterDept = filterByStaffDept ? resolveStaffDeptId(staffId) : null;
        List<ExamLabItemRowVo> list = dmsNonDrugItemRecordMapper.selectExamLabQueue(m, filterDept);
        return Result.success("查询成功", list);
    }

    private static final int WORKBENCH_DEFAULT_LIMIT = 100;
    private static final int WORKBENCH_SEARCH_LIMIT = 400;
    private static final int WORKBENCH_MAX_LIMIT = 500;

    @Override
    public Result<List<ExamLabItemRowVo>> listWorkbench(String token, String keyword, boolean filterByStaffDept, Integer limit) {
        Long staffId = requireStaffId(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        Long filterDept = filterByStaffDept ? resolveStaffDeptId(staffId) : null;
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        int lim = resolveWorkbenchLimit(limit, kw);
        List<ExamLabItemRowVo> list = dmsNonDrugItemRecordMapper.selectMedTechWorkbench(kw, filterDept, lim);
        return Result.success("查询成功", list);
    }

    private static int resolveWorkbenchLimit(Integer requested, String keywordTrimmed) {
        if (requested != null && requested > 0) {
            return Math.min(requested, WORKBENCH_MAX_LIMIT);
        }
        return keywordTrimmed == null ? WORKBENCH_DEFAULT_LIMIT : WORKBENCH_SEARCH_LIMIT;
    }

    @Override
    @Transactional
    public Result<Object> executeItem(Long itemId, String token) {
        Long staffId = requireStaffId(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (itemId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(itemId);
        if (rec == null) {
            return Result.error(ResultCode.PARAM_ERROR, "记录不存在");
        }
        if (rec.getType() == null || (rec.getType() != 1 && rec.getType() != 3)) {
            return Result.error(ResultCode.PARAM_ERROR, "非检查/检验项目");
        }
        if (rec.getStatus() != null && rec.getStatus() == 2) {
            return Result.error(ResultCode.PARAM_ERROR, "记录已作废");
        }
        if (rec.getStatus() != null && rec.getStatus() == 1) {
            return Result.error(ResultCode.PARAM_ERROR, "已登记，请勿重复操作");
        }
        if (!bmsCashierService.isNonDrugItemPaid(itemId)) {
            return Result.error(ResultCode.PARAM_ERROR, "患者尚未缴纳该检查/检验费用，请其至收费处缴费后再执行登记");
        }
        Integer n = dmsNonDrugItemRecordMapper.updateExecuteById(itemId, staffId, LocalDateTime.now());
        if (n == null || n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "执行失败");
        }
        return Result.success("登记成功");
    }

    @Override
    @Transactional
    public Result<Object> saveResult(Long itemId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (itemId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(itemId);
        if (rec == null) {
            return Result.error(ResultCode.PARAM_ERROR, "记录不存在");
        }
        if (rec.getStatus() == null || rec.getStatus() != 1) {
            return Result.error(ResultCode.PARAM_ERROR, "请先完成执行登记后再录入结果");
        }
        String checkResult = body == null ? null : asString(body.get("checkResult"));
        String clinicalImpression = body == null ? null : asString(body.get("clinicalImpression"));
        String clinicalDiagnosis = body == null ? null : asString(body.get("clinicalDiagnosis"));
        if (checkResult == null || checkResult.trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "检查/检验结果不能为空");
        }
        String resultImgUrlList;
        if (body != null && body.containsKey("resultImgUrlList")) {
            resultImgUrlList = normalizeImgUrlList(body.get("resultImgUrlList"));
        } else {
            resultImgUrlList = rec.getResultImgUrlList();
        }
        Integer n = dmsNonDrugItemRecordMapper.updateResultById(
                itemId,
                staffId,
                LocalDateTime.now(),
                checkResult.trim(),
                clinicalImpression == null ? null : clinicalImpression.trim(),
                clinicalDiagnosis == null ? null : clinicalDiagnosis.trim(),
                resultImgUrlList
        );
        if (n == null || n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "保存结果失败");
        }
        return Result.success("结果已保存");
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String normalizeImgUrlList(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (Collection<?>) raw) {
                if (o == null) {
                    continue;
                }
                String s = String.valueOf(o).trim();
                if (s.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(s);
            }
            return sb.length() == 0 ? null : sb.toString();
        }
        String s = String.valueOf(raw).trim();
        return s.isEmpty() ? null : s;
    }
}
