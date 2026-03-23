package com.his.service.impl;

import com.his.domain.BmsInvoiceRecord;
import com.his.domain.BmsPayableItem;
import com.his.domain.BmsPaymentAllocation;
import com.his.domain.BmsPaymentRecord;
import com.his.domain.DmsNonDrugItemRecord;
import com.his.domain.DmsRegistration;
import com.his.enums.PayableItemType;
import com.his.exception.BusinessException;
import com.his.enums.ResultCode;
import com.his.mapper.BmsInvoiceRecordMapper;
import com.his.mapper.BmsPayableItemMapper;
import com.his.mapper.BmsPaymentAllocationMapper;
import com.his.mapper.BmsPaymentRecordMapper;
import com.his.mapper.DmsNonDrugItemRecordMapper;
import com.his.mapper.DmsRegistrationMapper;
import com.his.service.IBmsCashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BmsCashierService implements IBmsCashierService {

    @Autowired
    private BmsPayableItemMapper bmsPayableItemMapper;

    @Autowired
    private BmsPaymentRecordMapper bmsPaymentRecordMapper;

    @Autowired
    private BmsPaymentAllocationMapper bmsPaymentAllocationMapper;

    @Autowired
    private DmsRegistrationMapper dmsRegistrationMapper;

    @Autowired
    private DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BmsInvoiceRecordMapper bmsInvoiceRecordMapper;

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * MySQL：UPDATE ... SET current_value = LAST_INSERT_ID(current_value + 1)，再 SELECT LAST_INSERT_ID() 取新号。
     */
    private long allocateNextInvoiceNo() {
        int updated = jdbcTemplate.update(
                "UPDATE bms_invoice_sequence SET current_value = LAST_INSERT_ID(current_value + 1) WHERE seq_name = ?",
                "GLOBAL");
        if (updated != 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR,
                    "发票序列未初始化，请执行 document/sql/bms_invoice_sequence.sql 后重启服务");
        }
        Long v = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (v == null) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "生成发票号失败");
        }
        return v;
    }

    private long insertInvoiceForPayment(Long paymentId,
                                         Long registrationId,
                                         BigDecimal total,
                                         Long operatorId,
                                         List<BmsPayableItem> settleLines) {
        long invoiceNo = allocateNextInvoiceNo();
        String itemList = settleLines.stream()
                .map(BmsPayableItem::getItemName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
        if (itemList.length() > 300) {
            itemList = itemList.substring(0, 300);
        }
        LocalDateTime now = LocalDateTime.now();
        BmsInvoiceRecord rec = new BmsInvoiceRecord();
        rec.setCreateTime(now);
        rec.setInvoiceNo(invoiceNo);
        rec.setBillId(paymentId);
        rec.setAmount(total);
        rec.setFreezeStatus(0);
        rec.setAssociateId(registrationId);
        rec.setOperatorId(operatorId);
        rec.setSettlementCatId(null);
        rec.setSettleRecordId(null);
        rec.setItemList(itemList.isEmpty() ? null : itemList);
        rec.setType(1);
        bmsInvoiceRecordMapper.insert(rec);
        return invoiceNo;
    }

    @Override
    @Transactional
    public void createRegistrationFeePayable(Long registrationId, BigDecimal amount) {
        if (registrationId == null) {
            return;
        }
        BmsPayableItem exist = bmsPayableItemMapper.selectByRegistrationTypeSource(
                registrationId, PayableItemType.REGISTRATION_FEE, registrationId);
        if (exist != null) {
            return;
        }
        BigDecimal amt = scaleMoney(amount);
        if (amt.compareTo(BigDecimal.ZERO) < 0) {
            amt = BigDecimal.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        BmsPayableItem row = new BmsPayableItem();
        row.setRegistrationId(registrationId);
        row.setItemType(PayableItemType.REGISTRATION_FEE);
        row.setSourceId(registrationId);
        row.setItemName("挂号费");
        row.setAmount(amt);
        row.setPaidAmount(BigDecimal.ZERO);
        row.setStatus(0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        bmsPayableItemMapper.insert(row);
    }

    @Override
    @Transactional
    public void createNonDrugPayable(Long registrationId, Long nonDrugItemId, BigDecimal amount, String itemName) {
        if (registrationId == null || nonDrugItemId == null) {
            return;
        }
        BmsPayableItem exist = bmsPayableItemMapper.selectByRegistrationTypeSource(
                registrationId, PayableItemType.NON_DRUG, nonDrugItemId);
        if (exist != null) {
            return;
        }
        BigDecimal amt = scaleMoney(amount);
        LocalDateTime now = LocalDateTime.now();
        BmsPayableItem row = new BmsPayableItem();
        row.setRegistrationId(registrationId);
        row.setItemType(PayableItemType.NON_DRUG);
        row.setSourceId(nonDrugItemId);
        row.setItemName(itemName != null && !itemName.isEmpty() ? itemName : "检查/检验");
        row.setAmount(amt);
        row.setPaidAmount(BigDecimal.ZERO);
        row.setStatus(0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        bmsPayableItemMapper.insert(row);
    }

    @Override
    @Transactional
    public void createMedicinePrescriptionPayable(Long registrationId, Long prescriptionId, BigDecimal amount) {
        if (registrationId == null || prescriptionId == null) {
            return;
        }
        BmsPayableItem exist = bmsPayableItemMapper.selectByRegistrationTypeSource(
                registrationId, PayableItemType.MEDICINE_PRESCRIPTION, prescriptionId);
        if (exist != null) {
            return;
        }
        BigDecimal amt = scaleMoney(amount);
        LocalDateTime now = LocalDateTime.now();
        BmsPayableItem row = new BmsPayableItem();
        row.setRegistrationId(registrationId);
        row.setItemType(PayableItemType.MEDICINE_PRESCRIPTION);
        row.setSourceId(prescriptionId);
        row.setItemName("成药处方");
        row.setAmount(amt);
        row.setPaidAmount(BigDecimal.ZERO);
        row.setStatus(0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        bmsPayableItemMapper.insert(row);
        dmsRegistrationMapper.updateBindStatus(registrationId, 0);
    }

    @Override
    @Transactional
    public void createHerbalPrescriptionPayable(Long registrationId, Long prescriptionId, BigDecimal amount) {
        if (registrationId == null || prescriptionId == null) {
            return;
        }
        BmsPayableItem exist = bmsPayableItemMapper.selectByRegistrationTypeSource(
                registrationId, PayableItemType.HERBAL_PRESCRIPTION, prescriptionId);
        if (exist != null) {
            return;
        }
        BigDecimal amt = scaleMoney(amount);
        LocalDateTime now = LocalDateTime.now();
        BmsPayableItem row = new BmsPayableItem();
        row.setRegistrationId(registrationId);
        row.setItemType(PayableItemType.HERBAL_PRESCRIPTION);
        row.setSourceId(prescriptionId);
        row.setItemName("草药处方");
        row.setAmount(amt);
        row.setPaidAmount(BigDecimal.ZERO);
        row.setStatus(0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        bmsPayableItemMapper.insert(row);
        dmsRegistrationMapper.updateBindStatus(registrationId, 0);
    }

    @Override
    public List<BmsPayableItem> listPayables(Long registrationId) {
        if (registrationId == null) {
            return new ArrayList<>();
        }
        return bmsPayableItemMapper.selectByRegistrationId(registrationId);
    }

    @Override
    @Transactional
    public Long payPayableItems(Long registrationId, List<Long> payableItemIds, Long operatorId, Integer payMethod) {
        if (registrationId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        if (payableItemIds == null || payableItemIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要缴费的项目");
        }
        BigDecimal total = BigDecimal.ZERO;
        List<BmsPayableItem> settle = new ArrayList<>();
        for (Long pid : payableItemIds) {
            if (pid == null) {
                continue;
            }
            BmsPayableItem it = bmsPayableItemMapper.selectById(pid);
            if (it == null || !registrationId.equals(it.getRegistrationId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的收费项");
            }
            if (it.getStatus() != null && it.getStatus() == 1) {
                continue;
            }
            BigDecimal remain = nz(it.getAmount()).subtract(nz(it.getPaidAmount()));
            remain = scaleMoney(remain);
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                bmsPayableItemMapper.updatePaidProgress(it.getId(), nz(it.getAmount()), 1);
                continue;
            }
            total = total.add(remain);
            settle.add(it);
        }
        total = scaleMoney(total);
        if (settle.isEmpty()) {
            syncBindStatus(registrationId);
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        BmsPaymentRecord pay = new BmsPaymentRecord();
        pay.setRegistrationId(registrationId);
        pay.setTotalAmount(total);
        pay.setPayTime(now);
        pay.setOperatorId(operatorId);
        pay.setPayMethod(payMethod);
        pay.setRemark("分项收费");
        pay.setCreateTime(now);
        bmsPaymentRecordMapper.insert(pay);
        Long paymentId = pay.getId();
        List<BmsPaymentAllocation> allocs = new ArrayList<>();
        for (BmsPayableItem it : settle) {
            BigDecimal remain = nz(it.getAmount()).subtract(nz(it.getPaidAmount()));
            remain = scaleMoney(remain);
            BmsPaymentAllocation a = new BmsPaymentAllocation();
            a.setPaymentId(paymentId);
            a.setPayableItemId(it.getId());
            a.setAmount(remain);
            allocs.add(a);
            bmsPayableItemMapper.updatePaidProgress(it.getId(), nz(it.getAmount()), 1);
        }
        if (!allocs.isEmpty()) {
            bmsPaymentAllocationMapper.insertBatch(allocs);
        }
        long invoiceNo = insertInvoiceForPayment(paymentId, registrationId, total, operatorId, settle);
        syncBindStatus(registrationId);
        return invoiceNo;
    }

    @Override
    @Transactional
    public Long payAllUnpaid(Long registrationId, Long operatorId, Integer payMethod) {
        List<BmsPayableItem> all = listPayables(registrationId);
        if (all == null || all.isEmpty()) {
            dmsRegistrationMapper.updateBindStatus(registrationId, 1);
            return null;
        }
        List<Long> ids = all.stream()
                .filter(p -> p.getStatus() == null || p.getStatus() != 1)
                .map(BmsPayableItem::getId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            syncBindStatus(registrationId);
            return null;
        }
        return payPayableItems(registrationId, ids, operatorId, payMethod);
    }

    private void syncBindStatus(Long registrationId) {
        int unpaid = bmsPayableItemMapper.countUnpaidByRegistrationId(registrationId);
        dmsRegistrationMapper.updateBindStatus(registrationId, unpaid == 0 ? 1 : 0);
    }

    @Override
    public boolean isRegistrationFeePaid(Long registrationId) {
        if (registrationId == null) {
            return false;
        }
        List<BmsPayableItem> all = listPayables(registrationId);
        if (all == null || all.isEmpty()) {
            DmsRegistration r = dmsRegistrationMapper.selectById(registrationId);
            return r != null && r.getBindStatus() != null && r.getBindStatus() == 1;
        }
        BmsPayableItem regFee = bmsPayableItemMapper.selectByRegistrationTypeSource(
                registrationId, PayableItemType.REGISTRATION_FEE, registrationId);
        if (regFee == null) {
            DmsRegistration r = dmsRegistrationMapper.selectById(registrationId);
            return r != null && r.getBindStatus() != null && r.getBindStatus() == 1;
        }
        return regFee.getStatus() != null && regFee.getStatus() == 1;
    }

    @Override
    public boolean isNonDrugItemPaid(Long nonDrugItemRecordId) {
        if (nonDrugItemRecordId == null) {
            return false;
        }
        DmsNonDrugItemRecord rec = dmsNonDrugItemRecordMapper.selectById(nonDrugItemRecordId);
        if (rec == null || rec.getRegistrationId() == null) {
            return false;
        }
        BmsPayableItem p = bmsPayableItemMapper.selectByRegistrationTypeSource(
                rec.getRegistrationId(), PayableItemType.NON_DRUG, nonDrugItemRecordId);
        if (p == null) {
            DmsRegistration r = dmsRegistrationMapper.selectById(rec.getRegistrationId());
            return r != null && r.getBindStatus() != null && r.getBindStatus() == 1;
        }
        return p.getStatus() != null && p.getStatus() == 1;
    }

    @Override
    @Transactional
    public void resetRegistrationPayments(Long registrationId) {
        if (registrationId == null) {
            return;
        }
        List<Long> paymentIds = jdbcTemplate.query(
                "select id from bms_payment_record where registration_id = ?",
                (rs, i) -> rs.getLong(1),
                registrationId);
        if (paymentIds != null && !paymentIds.isEmpty()) {
            for (Long pid : paymentIds) {
                jdbcTemplate.update("delete from bms_payment_allocation where payment_id = ?", pid);
            }
            jdbcTemplate.update("delete from bms_payment_record where registration_id = ?", registrationId);
        }
        jdbcTemplate.update(
                "update bms_payable_item set paid_amount = 0, status = 0, update_time = now() where registration_id = ?",
                registrationId);
        dmsRegistrationMapper.updateBindStatus(registrationId, 0);
    }
}
