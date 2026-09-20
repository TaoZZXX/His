package com.his.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BmsDailySettlement {
    private Long id; private Long operatorId; private String operatorName;
    private LocalDateTime rangeStart; private LocalDateTime rangeEnd; private LocalDateTime reportTime;
    private Integer status; private LocalDateTime auditTime; private Long auditorId; private String auditorName;
    private BigDecimal amtRegistration; private BigDecimal amtExam; private BigDecimal amtLab; private BigDecimal amtMedicine; private BigDecimal amtHerbal; private BigDecimal amtTreatment; private BigDecimal amtTotal;
    private BigDecimal payCash; private BigDecimal payInsurance; private BigDecimal payBank; private BigDecimal payAlipay; private BigDecimal payWechat; private BigDecimal payCreditcard; private BigDecimal payOther; private BigDecimal payChannelTotal;
    private String invoiceNormal; private String invoiceRed; private String invoiceReprint; private String remark; private LocalDateTime createTime;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getOperatorId(){return operatorId;} public void setOperatorId(Long operatorId){this.operatorId=operatorId;}
    public String getOperatorName(){return operatorName;} public void setOperatorName(String operatorName){this.operatorName=operatorName;}
    public LocalDateTime getRangeStart(){return rangeStart;} public void setRangeStart(LocalDateTime rangeStart){this.rangeStart=rangeStart;}
    public LocalDateTime getRangeEnd(){return rangeEnd;} public void setRangeEnd(LocalDateTime rangeEnd){this.rangeEnd=rangeEnd;}
    public LocalDateTime getReportTime(){return reportTime;} public void setReportTime(LocalDateTime reportTime){this.reportTime=reportTime;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public LocalDateTime getAuditTime(){return auditTime;} public void setAuditTime(LocalDateTime auditTime){this.auditTime=auditTime;}
    public Long getAuditorId(){return auditorId;} public void setAuditorId(Long auditorId){this.auditorId=auditorId;}
    public String getAuditorName(){return auditorName;} public void setAuditorName(String auditorName){this.auditorName=auditorName;}
    public BigDecimal getAmtRegistration(){return amtRegistration;} public void setAmtRegistration(BigDecimal v){this.amtRegistration=v;}
    public BigDecimal getAmtExam(){return amtExam;} public void setAmtExam(BigDecimal v){this.amtExam=v;}
    public BigDecimal getAmtLab(){return amtLab;} public void setAmtLab(BigDecimal v){this.amtLab=v;}
    public BigDecimal getAmtMedicine(){return amtMedicine;} public void setAmtMedicine(BigDecimal v){this.amtMedicine=v;}
    public BigDecimal getAmtHerbal(){return amtHerbal;} public void setAmtHerbal(BigDecimal v){this.amtHerbal=v;}
    public BigDecimal getAmtTreatment(){return amtTreatment;} public void setAmtTreatment(BigDecimal v){this.amtTreatment=v;}
    public BigDecimal getAmtTotal(){return amtTotal;} public void setAmtTotal(BigDecimal v){this.amtTotal=v;}
    public BigDecimal getPayCash(){return payCash;} public void setPayCash(BigDecimal v){this.payCash=v;}
    public BigDecimal getPayInsurance(){return payInsurance;} public void setPayInsurance(BigDecimal v){this.payInsurance=v;}
    public BigDecimal getPayBank(){return payBank;} public void setPayBank(BigDecimal v){this.payBank=v;}
    public BigDecimal getPayAlipay(){return payAlipay;} public void setPayAlipay(BigDecimal v){this.payAlipay=v;}
    public BigDecimal getPayWechat(){return payWechat;} public void setPayWechat(BigDecimal v){this.payWechat=v;}
    public BigDecimal getPayCreditcard(){return payCreditcard;} public void setPayCreditcard(BigDecimal v){this.payCreditcard=v;}
    public BigDecimal getPayOther(){return payOther;} public void setPayOther(BigDecimal v){this.payOther=v;}
    public BigDecimal getPayChannelTotal(){return payChannelTotal;} public void setPayChannelTotal(BigDecimal v){this.payChannelTotal=v;}
    public String getInvoiceNormal(){return invoiceNormal;} public void setInvoiceNormal(String invoiceNormal){this.invoiceNormal=invoiceNormal;}
    public String getInvoiceRed(){return invoiceRed;} public void setInvoiceRed(String invoiceRed){this.invoiceRed=invoiceRed;}
    public String getInvoiceReprint(){return invoiceReprint;} public void setInvoiceReprint(String invoiceReprint){this.invoiceReprint=invoiceReprint;}
    public String getRemark(){return remark;} public void setRemark(String remark){this.remark=remark;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime createTime){this.createTime=createTime;}
}
