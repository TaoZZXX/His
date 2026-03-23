package com.his.enums;

/**
 * bms_payable_item.item_type：与 document/sql/bms_partial_payment.sql 一致
 */
public final class PayableItemType {

    private PayableItemType() {
    }

    /** 非药品（检查、检验等） */
    public static final int NON_DRUG = 1;
    /** 成药处方头 */
    public static final int MEDICINE_PRESCRIPTION = 2;
    /** 草药处方头 */
    public static final int HERBAL_PRESCRIPTION = 3;
    /** 挂号费 */
    public static final int REGISTRATION_FEE = 4;
    /** 其它 */
    public static final int OTHER = 5;
}
