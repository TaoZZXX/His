package com.his.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class BmsOperatorSettleRecord {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startDatetime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endDatetime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private Long cashierId;

    private Long invoiceNum;
    private Long rushInvoiceNum;
    private Long reprintInvoiceNum;

    private String startEndInvoiceIdStr;
    private String rushInvoiceIdListStr;
    private String reprintInvoiceIdListStr;

    private BigDecimal medicineAmount;
    private BigDecimal herbalAmount;
    private BigDecimal checkAmount;
    private BigDecimal dispositionAmount;
    private BigDecimal registrationAmount;
    private BigDecimal testAmount;
    private BigDecimal amount;

    private BigDecimal cashAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal bankCardAmount;
    private BigDecimal alipayAmount;
    private BigDecimal wechatAmount;
    private BigDecimal creditCardAmount;
    private BigDecimal otherAmount;

    private Long verifyOperatorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime verifyDatetime;
    private Integer verifyStatus;
}
