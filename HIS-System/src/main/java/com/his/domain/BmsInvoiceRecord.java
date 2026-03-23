package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BmsInvoiceRecord {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private Long invoiceNo;
    private Long billId;
    private BigDecimal amount;
    private Integer freezeStatus;
    private Long associateId;
    private Long operatorId;
    private Long settlementCatId;
    private Long settleRecordId;
    private String itemList;

    private Integer type;
}

