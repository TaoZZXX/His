package com.his.domain;

public class DmsHerbalItemRecord {
    private Long id; private Integer status; private Long prescriptionId; private String medicalAdvice; private String footnote; private Long drugId; private Long usageNum; private Integer usageNumUnit; private Long totalNum; private Long currentNum;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public Long getPrescriptionId(){return prescriptionId;} public void setPrescriptionId(Long prescriptionId){this.prescriptionId=prescriptionId;} public String getMedicalAdvice(){return medicalAdvice;} public void setMedicalAdvice(String medicalAdvice){this.medicalAdvice=medicalAdvice;}
    public String getFootnote(){return footnote;} public void setFootnote(String footnote){this.footnote=footnote;} public Long getDrugId(){return drugId;} public void setDrugId(Long drugId){this.drugId=drugId;}
    public Long getUsageNum(){return usageNum;} public void setUsageNum(Long usageNum){this.usageNum=usageNum;} public Integer getUsageNumUnit(){return usageNumUnit;} public void setUsageNumUnit(Integer usageNumUnit){this.usageNumUnit=usageNumUnit;}
    public Long getTotalNum(){return totalNum;} public void setTotalNum(Long totalNum){this.totalNum=totalNum;} public Long getCurrentNum(){return currentNum;} public void setCurrentNum(Long currentNum){this.currentNum=currentNum;}
}
