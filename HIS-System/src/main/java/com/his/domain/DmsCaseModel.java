package com.his.domain;

import lombok.Data;

@Data
public class DmsCaseModel {
    private Long id;

    /** 主诉模板 */
    private String chiefComplaint;

    /** 现病史模板 */
    private String historyOfPresentIllness;

    /** 治疗史模板 */
    private String historyOfTreatment;

    /** 既往史模板 */
    private String pastHistory;

    /** 过敏史模板 */
    private String allergies;

    /** 健康检查模板 */
    private String healthCheckup;

    /** 初步疾病ID列表模板（保留表字段拼写） */
    private String priliminaryDiseIdList;

    /** 初步疾病名称列表模板（保留表字段拼写） */
    private String priliminaryDiseStrList;

    /** 模板名称 */
    private String name;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}

