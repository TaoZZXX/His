package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DmsCaseHistory {
    private Long id;

    /** 主诉 */
    private String chiefComplaint;
    /** 现病史 */
    private String historyOfPresentIllness;
    /** 治疗史 */
    private String historyOfTreatment;
    /** 既往史 */
    private String pastHistory;
    /** 过敏史 */
    private String allergies;
    /** 健康检查情况 */
    private String healthCheckup;

    /** 关联挂号ID（外键） */
    private Long registrationId;
    /** 初步疾病名称列表（保留表字段拼写） */
    private String priliminaryDiseStrList;

    /** 就诊开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startDate;

    /** 患者姓名 */
    private String name;
    /** 性别（0-女 1-男） */
    private Integer gender;
    /** 年龄字符串，如 25岁 */
    private String ageStr;

    /** 检查项目列表 */
    private String checkStrList;
    /** 处理措施列表 */
    private String dispositionStrList;
    /** 中药处方列表 */
    private String herbalPrescriptionStrList;

    /** 记录创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 确诊疾病名称列表 */
    private String definiteDiseStrList;
    /** 患者ID */
    private Long patientId;
    /** 检验项目列表 */
    private String testStrList;
    /** 西药处方列表 */
    private String medicinePrescriptionStrList;
    /** 病历状态（如0-草稿 1-已完成） */
    private Integer status;
    /** 初步疾病ID列表（保留表字段拼写） */
    private String priliminaryDiseIdList;
    /** 检查结果 */
    private String checkResult;
    /** 检验结果 */
    private String testResult;
}

