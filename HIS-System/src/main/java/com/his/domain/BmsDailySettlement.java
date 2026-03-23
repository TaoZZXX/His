package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BmsDailySettlement {

    private Long id;
    private Long operatorId;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime rangeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime rangeEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reportTime;

    /** 0 未核对 1 已核对 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditTime;

    private Long auditorId;
    private String auditorName;

    private BigDecimal amtRegistration;
    private BigDecimal amtExam;
    private BigDecimal amtLab;
    private BigDecimal amtMedicine;
    private BigDecimal amtHerbal;
    private BigDecimal amtTreatment;
    private BigDecimal amtTotal;

    private BigDecimal payCash;
    private BigDecimal payInsurance;
    private BigDecimal payBank;
    private BigDecimal payAlipay;
    private BigDecimal payWechat;
    private BigDecimal payCreditcard;
    private BigDecimal payOther;
    private BigDecimal payChannelTotal;

    private String invoiceNormal;
    private String invoiceRed;
    private String invoiceReprint;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
