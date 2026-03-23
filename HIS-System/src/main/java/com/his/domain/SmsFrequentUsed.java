package com.his.domain;

import lombok.Data;

/**
 * 员工常用项表（sms_frequent_used）
 */
@Data
public class SmsFrequentUsed {

    private Long id;

    /** 常用疾病药品ID列表 */
    private String medicineDiseIdList;

    /** 常用处置ID列表 */
    private String dispositionIdList;

    /** 常用检验ID列表 */
    private String testIdList;

    /** 常用草药疾病ID列表 */
    private String herbalDiseIdList;

    /** 常用检查模板ID列表 */
    private String checkModelIdList;

    /** 常用处置模板ID列表 */
    private String dispositionModelIdList;

    /** 常用检验模板ID列表 */
    private String testModelIdList;

    /** 常用药品模板ID列表 */
    private String drugModelIdList;

    /** 员工ID */
    private Long staffId;

    /** 常用检查ID列表 */
    private String checkIdList;

    /** 常用药品ID列表 */
    private String drugIdList;
}
