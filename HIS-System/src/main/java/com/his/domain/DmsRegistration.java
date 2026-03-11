package com.his.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DmsRegistration {
    private Long id;
    private Long patientId;
    private LocalDate createTime;
    private Integer endAttendance;  /// 就诊状态
    private Integer status; /// 挂号状态
    private Long skdId;
    private Integer needBook;
    private Integer bindStatus;
    private Long deptId;
    private LocalDate attendanceDate;   /// 就诊日期
    private String patientAgeStr;   /// 30岁3月

}
