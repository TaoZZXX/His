package com.his.service.impl;

import com.his.domain.BmsDailySettlement;
import com.his.enums.PayableItemType;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.BmsDailySettlementMapper;
import com.his.service.IBmsDailySettlementService;
import com.his.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BmsDailySettlementService implements IBmsDailySettlementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BmsDailySettlementMapper bmsDailySettlementMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    private Long requireStaffId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "请先登录");
        }
        Long id = jwtUtil.getUserIdFromToken(token);
        if (id == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "登录已失效");
        }
        return id;
    }

    private String staffName(Long staffId) {
        if (staffId == null) {
            return "—";
        }
        try {
            String n = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(name, username, '') FROM sms_staff WHERE id = ? LIMIT 1",
                    String.class,
                    staffId
            );
            return n != null && !n.isEmpty() ? n : String.valueOf(staffId);
        } catch (Exception e) {
            return String.valueOf(staffId);
        }
    }

    private boolean isLabLike(String itemName) {
        if (itemName == null) {
            return false;
        }
        String s = itemName;
        return s.contains("检验") || s.contains("化验");
    }

    @Override
    @Transactional
    public BmsDailySettlement generate(String token, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        Long operatorId = requireStaffId(token);
        if (rangeStart == null || rangeEnd == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "统计起止时间不能为空");
        }
        if (rangeEnd.isBefore(rangeStart)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "截止时间不能早于开始时间");
        }

        BigDecimal amtReg = BigDecimal.ZERO;
        BigDecimal amtExam = BigDecimal.ZERO;
        BigDecimal amtLab = BigDecimal.ZERO;
        BigDecimal amtMed = BigDecimal.ZERO;
        BigDecimal amtHerb = BigDecimal.ZERO;
        BigDecimal amtTreat = BigDecimal.ZERO;

        String allocSql =
                "SELECT pi.item_type AS itemType, pi.item_name AS itemName, a.amount AS amt " +
                        "FROM bms_payment_allocation a " +
                        "INNER JOIN bms_payment_record pr ON pr.id = a.payment_id " +
                        "INNER JOIN bms_payable_item pi ON pi.id = a.payable_item_id " +
                        "WHERE pr.pay_time >= ? AND pr.pay_time <= ?";
        List<Map<String, Object>> allocRows = jdbcTemplate.queryForList(allocSql, rangeStart, rangeEnd);

        for (Map<String, Object> row : allocRows) {
            Object tObj = row.get("itemType");
            int t = tObj instanceof Number ? ((Number) tObj).intValue() : Integer.parseInt(String.valueOf(tObj));
            String name = row.get("itemName") != null ? row.get("itemName").toString() : "";
            BigDecimal amt = scale(new BigDecimal(row.get("amt").toString()));
            if (t == PayableItemType.REGISTRATION_FEE) {
                amtReg = amtReg.add(amt);
            } else if (t == PayableItemType.NON_DRUG) {
                if (isLabLike(name)) {
                    amtLab = amtLab.add(amt);
                } else {
                    amtExam = amtExam.add(amt);
                }
            } else if (t == PayableItemType.MEDICINE_PRESCRIPTION) {
                amtMed = amtMed.add(amt);
            } else if (t == PayableItemType.HERBAL_PRESCRIPTION) {
                amtHerb = amtHerb.add(amt);
            } else {
                amtTreat = amtTreat.add(amt);
            }
        }

        BigDecimal amtTotal = scale(amtReg.add(amtExam).add(amtLab).add(amtMed).add(amtHerb).add(amtTreat));

        BigDecimal payCash = BigDecimal.ZERO;
        BigDecimal payIns = BigDecimal.ZERO;
        BigDecimal payBank = BigDecimal.ZERO;
        BigDecimal payAli = BigDecimal.ZERO;
        BigDecimal payWx = BigDecimal.ZERO;
        BigDecimal payCc = BigDecimal.ZERO;
        BigDecimal payOth = BigDecimal.ZERO;

        String paySql =
                "SELECT pay_method AS m, COALESCE(SUM(total_amount),0) AS s FROM bms_payment_record " +
                        "WHERE pay_time >= ? AND pay_time <= ? GROUP BY pay_method";
        List<Map<String, Object>> payRows = jdbcTemplate.queryForList(paySql, rangeStart, rangeEnd);
        for (Map<String, Object> row : payRows) {
            Object mObj = row.get("m");
            if (mObj == null) {
                payOth = payOth.add(scale(new BigDecimal(row.get("s").toString())));
                continue;
            }
            int m = mObj instanceof Number ? ((Number) mObj).intValue() : Integer.parseInt(String.valueOf(mObj));
            BigDecimal s = scale(new BigDecimal(row.get("s").toString()));
            switch (m) {
                case 1:
                    payCash = payCash.add(s);
                    break;
                case 2:
                    payWx = payWx.add(s);
                    break;
                case 3:
                    payAli = payAli.add(s);
                    break;
                case 4:
                    payBank = payBank.add(s);
                    break;
                case 5:
                    payIns = payIns.add(s);
                    break;
                case 6:
                    payCc = payCc.add(s);
                    break;
                default:
                    payOth = payOth.add(s);
            }
        }

        BigDecimal payChannelTotal = scale(payCash.add(payIns).add(payBank).add(payAli).add(payWx).add(payCc).add(payOth));

        LocalDateTime now = LocalDateTime.now();
        BmsDailySettlement rec = new BmsDailySettlement();
        rec.setOperatorId(operatorId);
        rec.setOperatorName(staffName(operatorId));
        rec.setRangeStart(rangeStart);
        rec.setRangeEnd(rangeEnd);
        rec.setReportTime(now);
        rec.setStatus(0);
        rec.setAmtRegistration(scale(amtReg));
        rec.setAmtExam(scale(amtExam));
        rec.setAmtLab(scale(amtLab));
        rec.setAmtMedicine(scale(amtMed));
        rec.setAmtHerbal(scale(amtHerb));
        rec.setAmtTreatment(scale(amtTreat));
        rec.setAmtTotal(amtTotal);
        rec.setPayCash(scale(payCash));
        rec.setPayInsurance(scale(payIns));
        rec.setPayBank(scale(payBank));
        rec.setPayAlipay(scale(payAli));
        rec.setPayWechat(scale(payWx));
        rec.setPayCreditcard(scale(payCc));
        rec.setPayOther(scale(payOth));
        rec.setPayChannelTotal(payChannelTotal);
        rec.setInvoiceNormal("—");
        rec.setInvoiceRed("—");
        rec.setInvoiceReprint("—");
        rec.setRemark(null);
        rec.setCreateTime(now);

        bmsDailySettlementMapper.insert(rec);
        return bmsDailySettlementMapper.selectById(rec.getId());
    }

    @Override
    public List<BmsDailySettlement> listByReportRange(String token, LocalDateTime queryStart, LocalDateTime queryEnd) {
        requireStaffId(token);
        if (queryStart == null || queryEnd == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "查询日期范围不能为空");
        }
        return bmsDailySettlementMapper.selectByReportTimeRange(queryStart, queryEnd);
    }

    @Override
    public BmsDailySettlement getById(String token, Long id) {
        requireStaffId(token);
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "id 不能为空");
        }
        BmsDailySettlement row = bmsDailySettlementMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "日结记录不存在");
        }
        return row;
    }

    @Override
    @Transactional
    public void audit(String token, Long settlementId) {
        Long auditorId = requireStaffId(token);
        if (settlementId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "id 不能为空");
        }
        BmsDailySettlement row = bmsDailySettlementMapper.selectById(settlementId);
        if (row == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "日结记录不存在");
        }
        if (row.getStatus() != null && row.getStatus() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该日结已核对");
        }
        LocalDateTime now = LocalDateTime.now();
        bmsDailySettlementMapper.updateAudit(settlementId, 1, auditorId, staffName(auditorId), now);
    }
}
