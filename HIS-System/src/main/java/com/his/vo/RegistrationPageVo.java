package com.his.vo;

import com.his.enums.EndAttendanceCode;
import com.his.enums.RegistrationBindStatus;
import com.his.enums.RegistrationStatusCode;
import lombok.Data;

import java.time.LocalDate;

/**
 * 挂号分页列表展示用 VO
 */
@Data
public class RegistrationPageVo {

    private Long id;
    private Long patientId;
    private String patientName;

    /** 病历号 */
    private String medicalRecordNo;
    /** 男 / 女 */
    private String genderStr;
    private LocalDate dateOfBirth;

    private Long deptId;
    private String deptName;

    private Long doctorId;
    private String doctorName;

    private Long skdId;

    /**
     * 排班午别：sms_skd.noon
     * 上午=0，下午=1
     */
    private Integer noon;

    private LocalDate createTime;
    private LocalDate attendanceDate;

    private RegistrationStatusCode status;
    private String statusDesc;

    /** 就诊是否结束 */
    private EndAttendanceCode endAttendance;

    /** 收费状态（就诊结束后收银台） */
    private RegistrationBindStatus bindStatus;

    private String remarks;
}

