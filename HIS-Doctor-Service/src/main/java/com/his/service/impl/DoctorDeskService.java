package com.his.service.impl;

import com.his.domain.*;
import com.his.enums.EndAttendanceCode;
import com.his.enums.NoonCode;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.mapper.*;
import com.his.service.IDoctorDeskService;
import com.his.utils.JwtUtil;
import com.his.vo.OutpatientDeskVo;
import com.his.vo.OutpatientPatientVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoctorDeskService implements IDoctorDeskService {
    private final JwtUtil jwtUtil;
    private final DmsRegistrationMapper dmsRegistrationMapper;
    private final SmsStaffMapper smsStaffMapper;
    private final DmsCaseHistoryMapper dmsCaseHistoryMapper;
    private final DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;
    private final DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper;
    private final DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper;
    private final DmsHerbalPrescriptionRecordMapper dmsHerbalPrescriptionRecordMapper;
    private final DmsHerbalItemRecordMapper dmsHerbalItemRecordMapper;
    private final DmsDrugMapper dmsDrugMapper;
    private final DmsNonDrugMapper dmsNonDrugMapper;
    private final BmsPayableItemMapper bmsPayableItemMapper;
    public DoctorDeskService(JwtUtil jwtUtil, DmsRegistrationMapper dmsRegistrationMapper, SmsStaffMapper smsStaffMapper, DmsCaseHistoryMapper dmsCaseHistoryMapper, DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper, DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper, DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper, DmsHerbalPrescriptionRecordMapper dmsHerbalPrescriptionRecordMapper, DmsHerbalItemRecordMapper dmsHerbalItemRecordMapper, DmsDrugMapper dmsDrugMapper, DmsNonDrugMapper dmsNonDrugMapper, BmsPayableItemMapper bmsPayableItemMapper) {
        this.jwtUtil = jwtUtil; this.dmsRegistrationMapper = dmsRegistrationMapper; this.smsStaffMapper = smsStaffMapper; this.dmsCaseHistoryMapper = dmsCaseHistoryMapper; this.dmsNonDrugItemRecordMapper = dmsNonDrugItemRecordMapper; this.dmsMedicinePrescriptionRecordMapper = dmsMedicinePrescriptionRecordMapper; this.dmsMedicineItemRecordMapper = dmsMedicineItemRecordMapper; this.dmsHerbalPrescriptionRecordMapper = dmsHerbalPrescriptionRecordMapper; this.dmsHerbalItemRecordMapper = dmsHerbalItemRecordMapper; this.dmsDrugMapper = dmsDrugMapper; this.dmsNonDrugMapper = dmsNonDrugMapper; this.bmsPayableItemMapper = bmsPayableItemMapper;
    }

    @Override
    public Result<OutpatientDeskVo> listPatients(String token, String scope, String keyword, String date, String session) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        Integer deptId = smsStaffMapper.selectDeptIdById(staffId);
        if (deptId == null) return Result.error(ResultCode.PARAM_ERROR, "无法获取医生科室信息");
        LocalDate attendanceDate = (date == null || date.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(date.trim());
        Integer noon = parseNoon(session);
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String queryScope = (scope == null || scope.trim().isEmpty()) ? "self" : scope;
        List<OutpatientPatientVo> all = dmsRegistrationMapper.selectOutpatientDeskPatients(staffId, deptId.longValue(), queryScope, attendanceDate, noon, EndAttendanceCode.UNFINISHED.getCode(), RegistrationStatusCode.CANCELED.getCode(), kw);
        List<OutpatientPatientVo> waiting = new ArrayList<>(), doing = new ArrayList<>();
        if (all != null) for (OutpatientPatientVo p : all) if (p != null) { if (p.getStatus() == RegistrationStatusCode.UNFINISHED) waiting.add(p); else doing.add(p); }
        OutpatientDeskVo vo = new OutpatientDeskVo(); vo.setWaitingPatients(waiting); vo.setDoingPatients(doing);
        return Result.success("查询成功", vo);
    }

    @Override
    public Result<Object> startVisit(Long id, String token, String scope) {
        if (id == null) return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!isRegistrationFeePaid(id)) return Result.error(ResultCode.PARAM_ERROR, "请先至门诊挂号工作台缴纳挂号费后，再开始诊疗");
        String sc = (scope == null || scope.trim().isEmpty()) ? "self" : scope.trim();
        Integer rows = "dept".equalsIgnoreCase(sc)
                ? dmsRegistrationMapper.startVisitByDeptId(id, requireDeptId(staffId), RegistrationStatusCode.FINISHED.getCode(), RegistrationStatusCode.UNFINISHED.getCode(), EndAttendanceCode.UNFINISHED.getCode(), RegistrationStatusCode.CANCELED.getCode())
                : dmsRegistrationMapper.startVisitById(id, staffId, RegistrationStatusCode.FINISHED.getCode(), RegistrationStatusCode.UNFINISHED.getCode(), EndAttendanceCode.UNFINISHED.getCode(), RegistrationStatusCode.CANCELED.getCode());
        return (rows == null || rows <= 0) ? Result.error(ResultCode.SERVER_ERROR, "开始诊疗失败或已开始") : Result.success("开始诊疗成功");
    }

    @Override
    public Result<Object> finishVisit(Long id, String token, String scope) {
        if (id == null) return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        String sc = (scope == null || scope.trim().isEmpty()) ? "self" : scope.trim();
        Integer rows = "dept".equalsIgnoreCase(sc)
                ? dmsRegistrationMapper.finishVisitByDeptId(id, requireDeptId(staffId), EndAttendanceCode.FINISHED.getCode(), RegistrationStatusCode.FINISHED.getCode(), EndAttendanceCode.UNFINISHED.getCode(), RegistrationStatusCode.CANCELED.getCode())
                : dmsRegistrationMapper.finishVisitById(id, staffId, EndAttendanceCode.FINISHED.getCode(), RegistrationStatusCode.FINISHED.getCode(), EndAttendanceCode.UNFINISHED.getCode(), RegistrationStatusCode.CANCELED.getCode());
        return (rows == null || rows <= 0) ? Result.error(ResultCode.SERVER_ERROR, "结束就诊失败或已结束") : Result.success("结束就诊成功");
    }

    @Override
    public Result<Map<String, Object>> getPatientContext(Long registrationId, String token) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        DmsCaseHistory caseHistory = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        List<DmsNonDrugItemRecord> examItems = dmsNonDrugItemRecordMapper.selectByRegistrationIdAndType(registrationId, 1);
        List<DmsNonDrugItemRecord> labItems = dmsNonDrugItemRecordMapper.selectByRegistrationIdAndType(registrationId, 3);
        List<DmsMedicinePrescriptionRecord> medicinePres = dmsMedicinePrescriptionRecordMapper.selectByRegistrationId(registrationId);
        List<Map<String, Object>> medicineWithItems = new ArrayList<>();
        if (medicinePres != null) for (DmsMedicinePrescriptionRecord p : medicinePres) {
            List<DmsMedicineItemRecord> items = dmsMedicineItemRecordMapper.selectByPrescriptionId(p.getId());
            List<Map<String, Object>> itemVos = new ArrayList<>();
            if (items != null) for (DmsMedicineItemRecord it : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", it.getId()); m.put("drugId", it.getDrugId()); m.put("qty", it.getNum()); m.put("usageMeans", it.getUsageMeans()); m.put("frequency", it.getFrequency()); m.put("medicalAdvice", it.getMedicalAdvice());
                DmsDrug drug = it.getDrugId() == null ? null : dmsDrugMapper.selectById(it.getDrugId());
                if (drug != null) { m.put("name", drug.getName()); m.put("spec", drug.getFormat()); m.put("unitPrice", drug.getPrice()); }
                itemVos.add(m);
            }
            Map<String, Object> pv = new HashMap<>(); pv.put("id", p.getId()); pv.put("amount", p.getAmount()); pv.put("createTime", p.getCreateTime()); pv.put("items", itemVos); medicineWithItems.add(pv);
        }
        List<DmsHerbalPrescriptionRecord> herbalPres = dmsHerbalPrescriptionRecordMapper.selectByRegistrationId(registrationId);
        List<Map<String, Object>> herbalWithItems = new ArrayList<>();
        if (herbalPres != null) for (DmsHerbalPrescriptionRecord p : herbalPres) {
            List<DmsHerbalItemRecord> items = dmsHerbalItemRecordMapper.selectByPrescriptionId(p.getId());
            Map<String, Object> pv = new HashMap<>(); pv.put("id", p.getId()); pv.put("amount", p.getAmount()); pv.put("therapy", p.getTherapy()); pv.put("medicalAdvice", p.getMedicalAdvice()); pv.put("createTime", p.getCreateTime()); pv.put("items", items == null ? Collections.emptyList() : items); herbalWithItems.add(pv);
        }
        BigDecimal examAmount = defaultZero(dmsNonDrugItemRecordMapper.sumAmountByRegistrationId(registrationId));
        BigDecimal medicineAmount = defaultZero(dmsMedicinePrescriptionRecordMapper.sumAmountByRegistrationId(registrationId));
        BigDecimal herbalAmount = defaultZero(dmsHerbalPrescriptionRecordMapper.sumAmountByRegistrationId(registrationId));
        Map<String, Object> billSummary = new HashMap<>();
        billSummary.put("examAmount", scaleMoney(examAmount)); billSummary.put("medicineAmount", scaleMoney(medicineAmount)); billSummary.put("herbalAmount", scaleMoney(herbalAmount)); billSummary.put("totalAmount", scaleMoney(examAmount.add(medicineAmount).add(herbalAmount)));
        Map<String, Object> data = new HashMap<>();
        data.put("caseHistory", caseHistory); data.put("examItems", examItems); data.put("labItems", labItems); data.put("diagnosis", caseHistory == null ? null : caseHistory.getDefiniteDiseStrList()); data.put("diagnosisBasis", caseHistory == null ? null : caseHistory.getCheckResult());
        data.put("medicinePrescriptions", medicineWithItems); data.put("herbalPrescriptions", herbalWithItems); data.put("billSummary", billSummary); data.put("billLines", Collections.emptyList());
        return Result.success("查询成功", data);
    }

    @Override public Result<List<DmsDrug>> listMedicineDict(String token, String keyword) { Long staffId = requireStaffId(token); return staffId == null ? Result.error(ResultCode.TOKEN_INVALID, "token 无效") : Result.success("查询成功", dmsDrugMapper.selectEnabledByKeyword(keyword)); }
    @Override public Result<List<DmsNonDrug>> listNonDrugDict(String token, Integer type, String keyword) { Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效"); if (type == null) return Result.error(ResultCode.PARAM_ERROR, "type 不能为空"); return Result.success("查询成功", dmsNonDrugMapper.selectEnabledByTypeAndKeyword(type, keyword)); }

    @Override @Transactional
    public Result<Object> saveCaseHistory(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        DmsCaseHistory ch = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        if (ch == null) { ch = new DmsCaseHistory(); ch.setRegistrationId(registrationId); fillPatientBase(ch, registrationId); ch.setCreateTime(LocalDateTime.now()); ch.setStatus(1); applyCaseHistoryFields(ch, body); dmsCaseHistoryMapper.insert(ch); }
        else { applyCaseHistoryFields(ch, body); dmsCaseHistoryMapper.updateByRegistrationId(ch); }
        return Result.success("保存病历成功");
    }
    @Override @Transactional
    public Result<Object> saveDiagnosis(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        String diagnosis = asString(body.get("diagnosis")), basis = asString(body.get("basis"));
        DmsCaseHistory ch = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        if (ch == null) { ch = new DmsCaseHistory(); ch.setRegistrationId(registrationId); fillPatientBase(ch, registrationId); ch.setCreateTime(LocalDateTime.now()); ch.setStatus(1); ch.setDefiniteDiseStrList(diagnosis); ch.setCheckResult(basis); dmsCaseHistoryMapper.insert(ch); }
        else { ch.setDefiniteDiseStrList(diagnosis); ch.setCheckResult(basis); dmsCaseHistoryMapper.updateByRegistrationId(ch); }
        return Result.success("保存确诊成功");
    }
    @Override @Transactional
    public Result<Object> saveNonDrugItem(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        Integer type = asInteger(body.get("type")); Long noDrugId = asLong(body.get("noDrugId"));
        if (type == null || noDrugId == null) return Result.error(ResultCode.PARAM_ERROR, "type/noDrugId 不能为空");
        DmsNonDrug dict = dmsNonDrugMapper.selectById(noDrugId);
        if (dict == null || dict.getStatus() == null || dict.getStatus() != 1) return Result.error(ResultCode.PARAM_ERROR, "无效的非药品项目");
        if (dict.getRecordType() == null || !dict.getRecordType().equals(type)) return Result.error(ResultCode.PARAM_ERROR, "项目类型不匹配");
        DmsNonDrugItemRecord rec = new DmsNonDrugItemRecord(); rec.setRegistrationId(registrationId); rec.setStatus(0); rec.setType(type); rec.setNoDrugId(noDrugId); rec.setAim(asString(body.get("aim"))); rec.setDemand(asString(body.get("remark"))); rec.setCreateStaffId(staffId); rec.setCreateTime(LocalDateTime.now()); rec.setAmount(dict.getPrice());
        dmsNonDrugItemRecordMapper.insert(rec); createPayable(registrationId, 1, rec.getId(), dict.getPrice(), dict.getName()); return Result.success("保存成功");
    }
    @Override @Transactional
    public Result<Object> saveMedicinePrescription(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        Object itemsObj = body.get("items"); if (!(itemsObj instanceof List)) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        List<?> rawItems = (List<?>) itemsObj; if (rawItems.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        BigDecimal total = BigDecimal.ZERO; List<DmsMedicineItemRecord> itemRecords = new ArrayList<>();
        for (Object o : rawItems) {
            if (!(o instanceof Map)) continue; Map<?, ?> m = (Map<?, ?>) o;
            Long drugId = asLong(m.get("drugId")), qty = asLong(m.get("qty")); if (drugId == null || qty == null || qty <= 0) return Result.error(ResultCode.PARAM_ERROR, "drugId/qty 无效");
            DmsDrug drug = dmsDrugMapper.selectById(drugId); if (drug == null || drug.getStatus() == null || drug.getStatus() != 1) return Result.error(ResultCode.PARAM_ERROR, "无效药品");
            total = total.add(defaultZero(drug.getPrice()).multiply(BigDecimal.valueOf(qty)));
            DmsMedicineItemRecord it = new DmsMedicineItemRecord(); it.setDrugId(drugId); it.setStatus(0); it.setMedicineUsage(asInteger(m.get("medicineUsage")) == null ? 1 : asInteger(m.get("medicineUsage"))); it.setFrequency(asInteger(m.get("frequency"))); it.setDays(asLong(m.get("days")));
            it.setNum(qty); it.setMedicalAdvice(asString(m.get("medicalAdvice"))); it.setUsageNum(asLong(m.get("usageNum"))); it.setUsageMeans(asInteger(m.get("usageMeans"))); it.setUsageNumUnit(asInteger(m.get("usageNumUnit"))); it.setCurrentNum(qty); itemRecords.add(it);
        }
        DmsMedicinePrescriptionRecord pres = new DmsMedicinePrescriptionRecord(); pres.setStatus(1); pres.setCreateTime(LocalDateTime.now()); pres.setAmount(total); pres.setName(asString(body.get("name"))); pres.setRegistrationId(registrationId); pres.setRefundStatus(0L); pres.setType(1); pres.setCreateStaffId(staffId); dmsMedicinePrescriptionRecordMapper.insert(pres);
        for (DmsMedicineItemRecord it : itemRecords) it.setPrescriptionId(pres.getId()); dmsMedicineItemRecordMapper.insertBatch(itemRecords);
        createPayable(registrationId, 2, pres.getId(), total, "成药处方"); return Result.success("保存处方成功");
    }
    @Override @Transactional
    public Result<Object> saveHerbalPrescription(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token); if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        Object itemsObj = body.get("items"); if (!(itemsObj instanceof List)) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        List<?> rawItems = (List<?>) itemsObj; if (rawItems.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        BigDecimal total = BigDecimal.ZERO; List<DmsHerbalItemRecord> itemRecords = new ArrayList<>();
        for (Object o : rawItems) {
            if (!(o instanceof Map)) continue; Map<?, ?> m = (Map<?, ?>) o;
            Long drugId = asLong(m.get("drugId")), totalNum = asLong(m.get("totalNum")); if (drugId == null || totalNum == null || totalNum <= 0) return Result.error(ResultCode.PARAM_ERROR, "drugId/totalNum 无效");
            DmsDrug drug = dmsDrugMapper.selectById(drugId); if (drug == null || drug.getStatus() == null || drug.getStatus() != 1) return Result.error(ResultCode.PARAM_ERROR, "无效药品");
            total = total.add(defaultZero(drug.getPrice()).multiply(BigDecimal.valueOf(totalNum)));
            DmsHerbalItemRecord it = new DmsHerbalItemRecord(); it.setStatus(0); it.setDrugId(drugId); it.setMedicalAdvice(asString(m.get("medicalAdvice"))); it.setFootnote(asString(m.get("footnote"))); it.setUsageNum(asLong(m.get("usageNum"))); it.setUsageNumUnit(asInteger(m.get("usageNumUnit"))); it.setTotalNum(totalNum); it.setCurrentNum(totalNum); itemRecords.add(it);
        }
        DmsHerbalPrescriptionRecord pres = new DmsHerbalPrescriptionRecord(); pres.setStatus(1); pres.setCreateTime(LocalDateTime.now()); pres.setAmount(total); pres.setName(asString(body.get("name"))); pres.setTherapy(asString(body.get("therapy"))); pres.setTherapyDetails(asString(body.get("therapyDetails"))); pres.setMedicalAdvice(asString(body.get("medicalAdvice")));
        pres.setPairNum(asLong(body.get("pairNum"))); pres.setRegistrationId(registrationId); pres.setFrequency(asInteger(body.get("frequency"))); pres.setUsageMeans(asInteger(body.get("usageMeans"))); pres.setType(2); pres.setCreateStaffId(staffId); dmsHerbalPrescriptionRecordMapper.insert(pres);
        for (DmsHerbalItemRecord it : itemRecords) it.setPrescriptionId(pres.getId()); dmsHerbalItemRecordMapper.insertBatch(itemRecords);
        createPayable(registrationId, 3, pres.getId(), total, "草药处方"); return Result.success("保存草药处方成功");
    }

    private boolean isRegistrationFeePaid(Long registrationId) { Integer cnt = bmsPayableItemMapper.countPaidRegistrationFee(registrationId); return cnt != null && cnt > 0; }
    private void createPayable(Long registrationId, int itemType, Long sourceId, BigDecimal amount, String itemName) {
        BmsPayableItem exists = bmsPayableItemMapper.selectByRegistrationTypeSource(registrationId, itemType, sourceId);
        if (exists != null) return;
        BmsPayableItem row = new BmsPayableItem(); row.setRegistrationId(registrationId); row.setItemType(itemType); row.setSourceId(sourceId); row.setItemName(itemName); row.setAmount(defaultZero(amount)); row.setPaidAmount(BigDecimal.ZERO); row.setStatus(0); row.setCreateTime(LocalDateTime.now()); row.setUpdateTime(LocalDateTime.now()); bmsPayableItemMapper.insert(row);
    }
    private Long requireStaffId(String token) { return token == null || token.trim().isEmpty() ? null : jwtUtil.getUserIdFromToken(token); }
    private Long requireDeptId(Long staffId) { Integer deptId = smsStaffMapper.selectDeptIdById(staffId); return deptId == null ? null : deptId.longValue(); }
    private boolean canOperateRegistration(Long staffId, Long registrationId) { Integer cnt = dmsRegistrationMapper.countOperatePermission(registrationId, staffId); return cnt != null && cnt > 0; }
    private void fillPatientBase(DmsCaseHistory ch, Long registrationId) { Map<String, Object> row = dmsRegistrationMapper.selectPatientBaseByRegistrationId(registrationId); if (row == null) return; ch.setPatientId(asLong(row.get("patientId"))); ch.setName(asString(row.get("name"))); ch.setGender(asInteger(row.get("gender"))); Integer age = asInteger(row.get("age")); if (age != null) ch.setAgeStr(age + "岁"); ch.setStartDate(LocalDateTime.now()); }
    private void applyCaseHistoryFields(DmsCaseHistory ch, Map<String, Object> body) { if (body == null) return; ch.setChiefComplaint(asString(body.get("chiefComplaint"))); ch.setHistoryOfPresentIllness(asString(body.get("historyOfPresentIllness"))); ch.setHistoryOfTreatment(asString(body.get("historyOfTreatment"))); ch.setPastHistory(asString(body.get("pastHistory"))); ch.setAllergies(asString(body.get("allergies"))); ch.setHealthCheckup(asString(body.get("healthCheckup"))); ch.setStartDate(parseDateTime(body.get("startDate"))); }
    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private Long asLong(Object o) { if (o == null) return null; if (o instanceof Number) return ((Number) o).longValue(); try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; } }
    private Integer asInteger(Object o) { if (o == null) return null; if (o instanceof Number) return ((Number) o).intValue(); try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; } }
    private LocalDateTime parseDateTime(Object o) { if (o == null) return null; String s = String.valueOf(o).trim(); if (s.isEmpty()) return null; try { if (s.length() == 10) return LocalDate.parse(s).atStartOfDay(); if (s.contains("T")) { if (s.length() >= 19) s = s.substring(0, 19); return LocalDateTime.parse(s.replace(" ", "T")); } return LocalDateTime.parse(s.replace(" ", "T")); } catch (Exception e) { return null; } }
    private BigDecimal defaultZero(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static BigDecimal scaleMoney(BigDecimal v) { return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP); }
    private Integer parseNoon(String session) { if (session == null || session.trim().isEmpty()) return null; if ("上午".equals(session)) return NoonCode.MORNING.getCode(); if ("下午".equals(session)) return NoonCode.AFTERNOON.getCode(); return null; }
}
