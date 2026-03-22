package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款流水——表 bms_payment_record
 */
@Data
public class BmsPaymentRecord {

    private Long id;

    private Long registrationId;

    /** 本次实收合计 */
    private BigDecimal totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    private Long operatorId;

    /**
     * 支付方式：1 现金 2 微信 3 支付宝 4 银行卡 9 其它
     */
    private Integer payMethod;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
