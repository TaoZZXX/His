package com.his.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BmsPayableItem {
    private Long id; private Long registrationId; private Integer itemType; private Long sourceId; private String itemName;
    private BigDecimal amount; private BigDecimal paidAmount; private Integer status; private LocalDateTime createTime; private LocalDateTime updateTime;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getRegistrationId(){return registrationId;} public void setRegistrationId(Long registrationId){this.registrationId=registrationId;}
    public Integer getItemType(){return itemType;} public void setItemType(Integer itemType){this.itemType=itemType;} public Long getSourceId(){return sourceId;} public void setSourceId(Long sourceId){this.sourceId=sourceId;}
    public String getItemName(){return itemName;} public void setItemName(String itemName){this.itemName=itemName;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
    public BigDecimal getPaidAmount(){return paidAmount;} public void setPaidAmount(BigDecimal paidAmount){this.paidAmount=paidAmount;} public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime createTime){this.createTime=createTime;} public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime updateTime){this.updateTime=updateTime;}
}
