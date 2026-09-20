package com.his.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DmsMedicinePrescriptionRecord {
    private Long id; private Integer status; private LocalDateTime createTime; private BigDecimal amount; private String name; private Long registrationId; private Long refundStatus; private Integer type; private Long createStaffId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime createTime){this.createTime=createTime;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public Long getRegistrationId(){return registrationId;} public void setRegistrationId(Long registrationId){this.registrationId=registrationId;}
    public Long getRefundStatus(){return refundStatus;} public void setRefundStatus(Long refundStatus){this.refundStatus=refundStatus;} public Integer getType(){return type;} public void setType(Integer type){this.type=type;}
    public Long getCreateStaffId(){return createStaffId;} public void setCreateStaffId(Long createStaffId){this.createStaffId=createStaffId;}
}
