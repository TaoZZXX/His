package com.his.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 检查检验工作台列表行（登记执行 / 结果录入）
 */
@Data
public class ExamLabItemRowVo {

    private Long id;
    private Long registrationId;
    /** 1-检查 3-检验 */
    private Integer type;
    /** 0 待执行 1 已执行 */
    private Integer status;

    private String demand;
    private Long noDrugId;
    private String checkResult;
    private String clinicalImpression;
    private String clinicalDiagnosis;

    private String patientName;
    private String medicalRecordNo;
    /** 就诊日计算年龄（岁） */
    private Integer age;
    /** 男/女 */
    private String genderStr;
    private String projectName;
    private LocalDate attendanceDate;
    private String deptName;

    /** 开立医生（create_staff_id） */
    private String orderDoctorName;

    /** 结果图片 URL 列表（逗号分隔，回显用） */
    private String resultImgUrlList;
}
