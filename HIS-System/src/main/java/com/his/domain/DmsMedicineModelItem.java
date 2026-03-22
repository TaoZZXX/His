package com.his.domain;

import lombok.Data;

/**
 * 西药模板明细项表（dms_medicine_model_item）
 */
@Data
public class DmsMedicineModelItem {

    private Long id;

    /** 关联模板 ID */
    private Long modelId;

    /** 状态（0 禁用 / 1 启用） */
    private Integer status;

    /** 药品 ID（西药） */
    private Long drugId;

    /** 用药用途 */
    private Integer medicineUsage;

    /** 用药频次 */
    private Integer frequency;

    /** 用药天数 */
    private Long days;

    /** 总数量 */
    private Long num;

    /** 医嘱 */
    private String medicalAdvice;

    /** 单次用量 */
    private Long usageNum;

    /** 用药方式 */
    private Integer usageMeans;

    /** 用量单位 */
    private Integer usageNumUnit;
}
