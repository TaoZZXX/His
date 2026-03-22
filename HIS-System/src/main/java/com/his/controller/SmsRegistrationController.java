package com.his.controller;

import com.his.domain.Result;
import com.his.domain.SmsDept;
import com.his.domain.PmsPatient;
import com.his.dto.RegistrationDto;
import com.his.enums.ResultCode;
import com.his.domain.BmsPayableItem;
import com.his.service.IBmsCashierService;
import com.his.service.IDmsRegistrationService;
import com.his.service.IPmsPatientService;
import com.his.domain.SmsRegistrationRank;
import com.his.mapper.SmsDeptMapper;
import com.his.mapper.SmsRegistrationRankMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.mapper.SmsSkdMapper;
import com.his.vo.StaffPageVo;
import com.his.enums.NoonCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registration")
public class SmsRegistrationController {

    @Autowired
    private SmsDeptMapper smsDeptMapper;

    @Autowired
    private SmsStaffMapper smsStaffMapper;

    @Autowired
    private SmsSkdMapper smsSkdMapper;

    @Autowired
    private IPmsPatientService iPmsPatientService;

    @Autowired
    private IDmsRegistrationService iDmsRegistrationService;

    @Autowired
    private IBmsCashierService bmsCashierService;

    @Autowired
    private SmsRegistrationRankMapper smsRegistrationRankMapper;

    /**
     * 挂号号别列表（普通/专家等，含价格）
     */
    @GetMapping("/registration-ranks")
    public Result<List<SmsRegistrationRank>> listRegistrationRanks() {
        return Result.success("查询成功", smsRegistrationRankMapper.selectAllEnabled());
    }

    /**
     * 获取科室列表
     */
    @GetMapping("/departments")
    public Result<List<SmsDept>> getDepartments() {
        List<SmsDept> list = smsDeptMapper.selectAllDepts();
        return Result.success("获取科室列表成功", list);
    }

    /**
     * 获取科室下医生列表
     */
    @GetMapping("/departments/{deptId}/doctors")
    public Result<List<StaffPageVo>> getDoctorsByDept(@PathVariable("deptId") Long deptId) {
        List<StaffPageVo> list = smsStaffMapper.selectStaffByDept(deptId);
        return Result.success("获取医生列表成功", list);
    }

    /**
     * 按排班规则查询某天可挂号医生列表
     */
    @GetMapping("/doctors/available")
    public Result<List<StaffPageVo>> listAvailableDoctors(@RequestParam("deptId") Long deptId,
                                                          @RequestParam("date") String date,
                                                          @RequestParam(value = "session", required = false) String session) {
        if (deptId == null || date == null) {
            return Result.error(ResultCode.PARAM_ERROR, "科室和日期不能为空");
        }
        java.time.LocalDate d = java.time.LocalDate.parse(date);
        java.time.LocalDateTime start = d.atStartOfDay();
        java.time.LocalDateTime end = d.plusDays(1).atStartOfDay();
        Integer noon = null;
        if ("上午".equals(session)) {
            noon = NoonCode.MORNING.getCode();
        } else if ("下午".equals(session)) {
            noon = NoonCode.AFTERNOON.getCode();
        }
        java.util.List<Long> staffIds = smsSkdMapper.selectAvailableStaffIds(deptId, start, end, noon);
        if (staffIds == null || staffIds.isEmpty()) {
            return Result.success("暂无可挂号医生", java.util.Collections.emptyList());
        }
        java.util.List<StaffPageVo> doctors = smsStaffMapper.selectStaffByIds(staffIds);
        return Result.success("查询成功", doctors);
    }

    /**
     * 根据身份证号查询患者信息（兼容前端调用 /sms/registration/patient ）
     */
    @GetMapping("/patient")
    public Result<PmsPatient> getPatientByIdentificationNo(@RequestParam("identificationNo") Long identificationNo) {
        if (identificationNo == null) {
            return Result.error(ResultCode.PARAM_ERROR, "身份证号不能为空");
        }
        PmsPatient patient = iPmsPatientService.selectPmsPatientByIdentificationNo(identificationNo);
        return Result.success("查询成功", patient);
    }

    /**
     * 创建挂号（前端 createRegistration 会调用此接口）
     */
    @PostMapping("/registrations")
    public Result<?> createRegistration(@RequestBody RegistrationDto registrationDto) {
        Long registrationId = iDmsRegistrationService.registration(registrationDto);
        Map<String, Object> data = new HashMap<>();
        data.put("registrationId", registrationId);
        return Result.success("挂号成功", data);
    }

    /**
     * 分项收费：查询某挂号的应收明细
     */
    @GetMapping("/registrations/{id}/payables")
    public Result<List<BmsPayableItem>> listPayables(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        return Result.success("查询成功", bmsCashierService.listPayables(id));
    }

    /**
     * 分项缴费：body 可传 { "payableItemIds": [1,2,3] }；不传或空数组则缴清该挂号下所有未付明细
     */
    @PostMapping("/registrations/{id}/pay-items")
    public Result<Map<String, Object>> paySelectedItems(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        java.util.List<Long> ids = extractPayableItemIds(body);
        Long invoiceNo;
        if (ids == null || ids.isEmpty()) {
            invoiceNo = bmsCashierService.payAllUnpaid(id, null, 1);
        } else {
            invoiceNo = bmsCashierService.payPayableItems(id, ids, null, 1);
        }
        Map<String, Object> data = new HashMap<>();
        if (invoiceNo != null) {
            data.put("invoiceNo", invoiceNo);
        }
        return Result.success("缴费成功", data);
    }

    /**
     * 更新挂号（编辑挂号信息）
     * 对应前端：PUT /sms/registration/registrations/{id}
     */
    @PutMapping("/registrations/{id}")
    public Result<?> updateRegistration(@PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> body) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        Long deptId = null;
        Long doctorId = null;
        LocalDate attendanceDate = null;
        String session = null;
        if (body != null) {
            Object deptIdObj = body.get("departmentId");
            if (deptIdObj instanceof Number) {
                deptId = ((Number) deptIdObj).longValue();
            }
            Object doctorIdObj = body.get("doctorId");
            if (doctorIdObj instanceof Number) {
                doctorId = ((Number) doctorIdObj).longValue();
            }
            Object dateObj = body.get("registrationDate");
            if (dateObj instanceof String) {
                String s = (String) dateObj;
                // 兼容带时间和时区的字符串，例如 2026-03-16T16:00:00.000Z
                if (s.length() >= 10) {
                    s = s.substring(0, 10);
                }
                attendanceDate = LocalDate.parse(s);
            }
            Object sessionObj = body.get("session");
            if (sessionObj instanceof String) {
                session = (String) sessionObj;
            }
        }
        iDmsRegistrationService.updateRegistrationBasic(id, deptId, doctorId, session, attendanceDate);
        return Result.success("更新挂号信息成功");
    }

    /**
     * 删除挂号记录
     * 对应前端：DELETE /sms/registration/registrations/{id}
     */
    @DeleteMapping("/registrations/{id}")
    public Result<?> deleteRegistration(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        iDmsRegistrationService.deleteRegistration(id);
        return Result.success("删除挂号记录成功");
    }

    /**
     * 退号（不物理删除）：把状态置为已取消
     * 对应前端：POST /sms/registration/registrations/{id}/cancel
     */
    @PostMapping("/registrations/{id}/cancel")
    public Result<?> cancelRegistration(@PathVariable("id") Long id,
                                          @RequestBody(required = false) Map<String, Object> body) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        iDmsRegistrationService.cancelRegistration(id);
        return Result.success("退号成功");
    }

    /**
     * 分页查询所有挂号记录，按挂号时间倒序
     */
    @PostMapping("/getAllByPage")
    public Result<Map<String, Object>> getAllByPage(@RequestBody Map<String, Object> body) {
        int page = 0;
        int size = 10;
        String keyword = null;
        if (body != null) {
            Object p = body.get("page");
            Object s = body.get("size");
            if (p instanceof Number) {
                page = ((Number) p).intValue();
            }
            if (s instanceof Number) {
                size = ((Number) s).intValue();
            }
            Object kw = body.get("keyword");
            if (kw instanceof String) {
                keyword = (String) kw;
            }
        }
        java.util.List<com.his.vo.RegistrationPageVo> list = iDmsRegistrationService.listByPage(page, size, keyword);
        long total = iDmsRegistrationService.countAll(keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success("查询挂号记录成功", data);
    }

    /**
     * 收银台：就诊结束后收费
     */
    @PostMapping("/registrations/{id}/pay")
    public Result<Map<String, Object>> payRegistration(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        Long invoiceNo = iDmsRegistrationService.markRegistrationPaid(id);
        Map<String, Object> data = new HashMap<>();
        if (invoiceNo != null) {
            data.put("invoiceNo", invoiceNo);
        }
        return Result.success("缴费成功", data);
    }

    private static java.util.List<Long> extractPayableItemIds(Map<String, Object> body) {
        if (body == null) {
            return null;
        }
        Object raw = body.get("payableItemIds");
        if (!(raw instanceof java.util.List)) {
            return null;
        }
        java.util.List<Long> out = new java.util.ArrayList<>();
        for (Object o : (java.util.List<?>) raw) {
            if (o instanceof Number) {
                out.add(((Number) o).longValue());
            } else if (o != null) {
                try {
                    out.add(Long.parseLong(String.valueOf(o)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return out;
    }

    /**
     * 收银台：退费（回退缴费状态）
     */
    @PostMapping("/registrations/{id}/refund")
    public Result<?> refundRegistration(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        iDmsRegistrationService.markRegistrationRefund(id);
        return Result.success("退费成功");
    }

}
