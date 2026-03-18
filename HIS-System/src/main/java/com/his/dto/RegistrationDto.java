package com.his.dto;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;


@Data
public class RegistrationDto {

    private Long id; // 患者 ID
    private Long identificationNo; // 患者身份证号/证件号码
    private String name; // 患者姓名
    private LocalDate birthDate; // 出生日期
    private String gender;
    private String address; // 家庭住址
    private String contact; // 联系电话
    private String department; // 科室名称
    private Long departmentId; // 科室ID
    private String level; // 就诊级别
    private LocalDate registrationDate; // 挂号日期
    private String payment; // 支付方式
    private String session; // 就诊时间段
    private String doctor; // 医生姓名
    private Long doctorId; // 医生ID
    private Double amount; // 挂号费用
    private String medicalRecord; // 病历编号
}
