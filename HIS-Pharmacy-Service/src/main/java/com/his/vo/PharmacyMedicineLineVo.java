package com.his.vo;

import java.math.BigDecimal;

public class PharmacyMedicineLineVo {
    private Long itemId; private Long prescriptionId; private String prescriptionName; private String drugName; private BigDecimal unitPrice;
    private Long num; private Integer frequency; private String medicalAdvice; private String orderDoctorName; private Long currentNum; private Long refundNum; private Integer itemStatus;
    public Long getItemId(){return itemId;} public void setItemId(Long itemId){this.itemId=itemId;} public Long getPrescriptionId(){return prescriptionId;} public void setPrescriptionId(Long prescriptionId){this.prescriptionId=prescriptionId;}
    public String getPrescriptionName(){return prescriptionName;} public void setPrescriptionName(String prescriptionName){this.prescriptionName=prescriptionName;} public String getDrugName(){return drugName;} public void setDrugName(String drugName){this.drugName=drugName;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal unitPrice){this.unitPrice=unitPrice;} public Long getNum(){return num;} public void setNum(Long num){this.num=num;}
    public Integer getFrequency(){return frequency;} public void setFrequency(Integer frequency){this.frequency=frequency;} public String getMedicalAdvice(){return medicalAdvice;} public void setMedicalAdvice(String medicalAdvice){this.medicalAdvice=medicalAdvice;}
    public String getOrderDoctorName(){return orderDoctorName;} public void setOrderDoctorName(String orderDoctorName){this.orderDoctorName=orderDoctorName;} public Long getCurrentNum(){return currentNum;} public void setCurrentNum(Long currentNum){this.currentNum=currentNum;}
    public Long getRefundNum(){return refundNum;} public void setRefundNum(Long refundNum){this.refundNum=refundNum;} public Integer getItemStatus(){return itemStatus;} public void setItemStatus(Integer itemStatus){this.itemStatus=itemStatus;}
}
