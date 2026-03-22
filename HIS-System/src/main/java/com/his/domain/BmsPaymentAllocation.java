package com.his.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收款分摊——表 bms_payment_allocation
 */
@Data
public class BmsPaymentAllocation {

    private Long id;

    private Long paymentId;

    private Long payableItemId;

    /** 本笔收款摊到该应收明细的金额 */
    private BigDecimal amount;
}
