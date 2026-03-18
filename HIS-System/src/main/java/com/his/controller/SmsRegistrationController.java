package com.his.controller;

import com.his.domain.Result;
import com.his.domain.SmsDept;
import com.his.domain.PmsPatient;
import com.his.dto.RegistrationDto;
import com.his.enums.ResultCode;
import com.his.service.IDmsRegistrationService;
import com.his.service.IPmsPatientService;
import com.his.mapper.SmsDeptMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.vo.StaffPageVo;
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
    private IPmsPatientService iPmsPatientService;

    @Autowired
    private IDmsRegistrationService iDmsRegistrationService;

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
        iDmsRegistrationService.registration(registrationDto);
        return Result.success("挂号成功");
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
     * 分页查询所有挂号记录，按挂号时间倒序
     */
    @PostMapping("/getAllByPage")
    public Result<Map<String, Object>> getAllByPage(@RequestBody Map<String, Object> body) {
        int page = 0;
        int size = 10;
        if (body != null) {
            Object p = body.get("page");
            Object s = body.get("size");
            if (p instanceof Number) {
                page = ((Number) p).intValue();
            }
            if (s instanceof Number) {
                size = ((Number) s).intValue();
            }
        }
        java.util.List<com.his.vo.RegistrationPageVo> list = iDmsRegistrationService.listByPage(page, size);
        long total = iDmsRegistrationService.countAll();
        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success("查询挂号记录成功", data);
    }


}
