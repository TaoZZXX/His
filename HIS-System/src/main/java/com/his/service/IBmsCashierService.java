package com.his.service;

import com.his.domain.BmsPayableItem;

import java.math.BigDecimal;
import java.util.List;

public interface IBmsCashierService {

    void createRegistrationFeePayable(Long registrationId, BigDecimal amount);

    void createNonDrugPayable(Long registrationId, Long nonDrugItemId, BigDecimal amount, String itemName);

    void createMedicinePrescriptionPayable(Long registrationId, Long prescriptionId, BigDecimal amount);

    void createHerbalPrescriptionPayable(Long registrationId, Long prescriptionId, BigDecimal amount);

    List<BmsPayableItem> listPayables(Long registrationId);

    /**
     * 缴清选中的应收行（本次按「整行剩余金额」一次结清）
     *
     * @return 本次收款对应发票号；未产生收款流水时返回 null
     */
    Long payPayableItems(Long registrationId, List<Long> payableItemIds, Long operatorId, Integer payMethod);

    /**
     * 缴清该挂号下所有未付明细，并同步 bind_status
     *
     * @return 本次收款对应发票号；未产生收款流水时返回 null
     */
    Long payAllUnpaid(Long registrationId, Long operatorId, Integer payMethod);

    boolean isRegistrationFeePaid(Long registrationId);

    boolean isNonDrugItemPaid(Long nonDrugItemRecordId);

    /** 退费：清空该挂号分项记录与流水，bind_status=0 */
    void resetRegistrationPayments(Long registrationId);
}
