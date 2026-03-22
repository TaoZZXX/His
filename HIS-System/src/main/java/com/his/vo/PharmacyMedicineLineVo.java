package com.his.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PharmacyMedicineLineVo {

    private Long itemId;
    private Long prescriptionId;
    private String prescriptionName;
    private String drugName;
    private BigDecimal unitPrice;
    private Long num;
    private Integer frequency;
    /** 用药医嘱 / 说明 */
    private String medicalAdvice;
    private String orderDoctorName;
    private Long currentNum;
    private Long refundNum;
    private Integer itemStatus;
}
