package com.his.vo;

import com.his.enums.EndAttendanceCode;
import com.his.enums.RegistrationStatusCode;
import lombok.Data;

@Data
public class OutpatientPatientVo {
    /**
     * 挂号记录 id（dms_registration.id）
     */
    private Long id;

    /**
     * 患者就诊号（pms_patient.medical_record_no）
     */
    private String medicalNo;

    private String name;

    /**
     * 性别：男/女
     */
    private String gender;

    /**
     * 年龄（按就诊日期计算）
     */
    private Integer age;

    /**
     * 挂号状态
     */
    private RegistrationStatusCode status;

    /**
     * 就诊状态
     */
    private EndAttendanceCode endAttendance;

    /**
     * 午别：0=上午，1=下午（来自 sms_skd.noon）
     */
    private Integer noon;
}

