package com.his.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DmsHerbalPrescriptionRecord {
    private Long id; private Integer status; private LocalDateTime createTime; private BigDecimal amount; private String name; private String therapy; private String therapyDetails;
    private String medicalAdvice; private Long pairNum; private Long registrationId; private Integer frequency; private Integer usageMeans; private Integer type; private Long createStaffId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime createTime){this.createTime=createTime;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public String getTherapy(){return therapy;} public void setTherapy(String therapy){this.therapy=therapy;}
    public String getTherapyDetails(){return therapyDetails;} public void setTherapyDetails(String therapyDetails){this.therapyDetails=therapyDetails;} public String getMedicalAdvice(){return medicalAdvice;} public void setMedicalAdvice(String medicalAdvice){this.medicalAdvice=medicalAdvice;}
    public Long getPairNum(){return pairNum;} public void setPairNum(Long pairNum){this.pairNum=pairNum;} public Long getRegistrationId(){return registrationId;} public void setRegistrationId(Long registrationId){this.registrationId=registrationId;}
    public Integer getFrequency(){return frequency;} public void setFrequency(Integer frequency){this.frequency=frequency;} public Integer getUsageMeans(){return usageMeans;} public void setUsageMeans(Integer usageMeans){this.usageMeans=usageMeans;}
    public Integer getType(){return type;} public void setType(Integer type){this.type=type;} public Long getCreateStaffId(){return createStaffId;} public void setCreateStaffId(Long createStaffId){this.createStaffId=createStaffId;}
}
