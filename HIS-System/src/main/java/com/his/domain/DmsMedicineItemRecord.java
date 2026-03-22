package com.his.domain;

import lombok.Data;

@Data
public class DmsMedicineItemRecord {
    private Long id;

    /** 关联药品ID */
    private Long drugId;

    /** 状态（0-正常 1-作废 2-已退款等） */
    private Integer status;

    /** 关联处方ID（西药/中药处方） */
    private Long prescriptionId;

    /** 用药用途（如1-治疗 2-预防等） */
    private Integer medicineUsage;

    /** 用药频次（如1-每日1次 2-每日2次） */
    private Integer frequency;

    /** 用药天数 */
    private Long days;

    /** 药品总数量 */
    private Long num;

    /** 用药医嘱 */
    private String medicalAdvice;

    /** 退款数量 */
    private Long refundNum;

    /** 单次用量 */
    private Long usageNum;

    /** 用药方式（如1-口服 2-外用 3-注射等） */
    private Integer usageMeans;

    /** 用量单位（如1-片 2-粒 3-袋 4-ml等） */
    private Integer usageNumUnit;

    /** 当前剩余数量 */
    private Long currentNum;
}

