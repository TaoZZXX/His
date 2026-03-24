package com.his.service.impl;

import com.his.domain.DmsCaseHistory;
import com.his.domain.DmsDrug;
import com.his.domain.DmsHerbalItemRecord;
import com.his.domain.DmsHerbalPrescriptionRecord;
import com.his.domain.DmsMedicineItemRecord;
import com.his.domain.DmsMedicinePrescriptionRecord;
import com.his.domain.DmsNonDrug;
import com.his.domain.DmsNonDrugItemRecord;
import com.his.domain.Result;
import com.his.enums.EndAttendanceCode;
import com.his.enums.NoonCode;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.mapper.DmsCaseHistoryMapper;
import com.his.mapper.DmsDrugMapper;
import com.his.mapper.DmsHerbalItemRecordMapper;
import com.his.mapper.DmsHerbalPrescriptionRecordMapper;
import com.his.mapper.DmsMedicineItemRecordMapper;
import com.his.mapper.DmsMedicinePrescriptionRecordMapper;
import com.his.mapper.DmsNonDrugItemRecordMapper;
import com.his.mapper.DmsNonDrugMapper;
import com.his.mapper.DmsRegistrationMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.service.IBmsCashierService;
import com.his.service.IDoctorDeskService;
import com.his.utils.JwtUtil;
import com.his.vo.OutpatientDeskVo;
import com.his.vo.OutpatientPatientVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorDeskService implements IDoctorDeskService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DmsRegistrationMapper dmsRegistrationMapper;

    @Autowired
    private SmsStaffMapper smsStaffMapper;

    @Autowired
    private DmsCaseHistoryMapper dmsCaseHistoryMapper;

    @Autowired
    private DmsNonDrugItemRecordMapper dmsNonDrugItemRecordMapper;

    @Autowired
    private DmsMedicinePrescriptionRecordMapper dmsMedicinePrescriptionRecordMapper;

    @Autowired
    private DmsMedicineItemRecordMapper dmsMedicineItemRecordMapper;

    @Autowired
    private DmsHerbalPrescriptionRecordMapper dmsHerbalPrescriptionRecordMapper;

    @Autowired
    private DmsHerbalItemRecordMapper dmsHerbalItemRecordMapper;

    @Autowired
    private DmsDrugMapper dmsDrugMapper;

    @Autowired
    private DmsNonDrugMapper dmsNonDrugMapper;

    @Autowired
    private IBmsCashierService bmsCashierService;

    @Override
    public Result<OutpatientDeskVo> listPatients(String token, String scope, String keyword, String date, String session) {
        Long staffId = requireStaffId(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }

        Integer deptId;
        try {
            deptId = smsStaffMapper.selectDeptIdById(staffId);
        } catch (Exception e) {
            deptId = null;
        }
        if (deptId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "无法获取医生科室信息");
        }

        LocalDate attendanceDate = (date == null || date.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(date.trim());
        Integer noon = parseNoon(session);
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String queryScope = (scope == null || scope.trim().isEmpty()) ? "self" : scope;

        List<OutpatientPatientVo> all = dmsRegistrationMapper.selectOutpatientDeskPatients(
                staffId,
                deptId.longValue(),
                queryScope,
                attendanceDate,
                noon,
                EndAttendanceCode.UNFINISHED.getCode(),
                RegistrationStatusCode.CANCELED.getCode(),
                kw
        );

        List<OutpatientPatientVo> waiting = new ArrayList<>();
        List<OutpatientPatientVo> doing = new ArrayList<>();
        if (all != null) {
            for (OutpatientPatientVo p : all) {
                if (p == null) continue;
                RegistrationStatusCode st = p.getStatus();
                if (st == RegistrationStatusCode.UNFINISHED) {
                    waiting.add(p);
                } else {
                    doing.add(p);
                }
            }
        }
        OutpatientDeskVo vo = new OutpatientDeskVo();
        vo.setWaitingPatients(waiting);
        vo.setDoingPatients(doing);
        return Result.success("查询成功", vo);
    }

    @Override
    public Result<Object> startVisit(Long id, String token, String scope) {
        if (id == null) return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!bmsCashierService.isRegistrationFeePaid(id)) {
            return Result.error(ResultCode.PARAM_ERROR, "请先至门诊挂号工作台缴纳挂号费后，再开始诊疗");
        }

        String sc = (scope == null || scope.trim().isEmpty()) ? "self" : scope.trim();
        Integer rows;
        if ("dept".equalsIgnoreCase(sc)) {
            Long deptId = requireDeptId(staffId);
            if (deptId == null) {
                return Result.error(ResultCode.PARAM_ERROR, "无法获取医生科室信息");
            }
            rows = dmsRegistrationMapper.startVisitByDeptId(
                    id,
                    deptId,
                    RegistrationStatusCode.FINISHED.getCode(),
                    RegistrationStatusCode.UNFINISHED.getCode(),
                    EndAttendanceCode.UNFINISHED.getCode(),
                    RegistrationStatusCode.CANCELED.getCode()
            );
        } else {
            rows = dmsRegistrationMapper.startVisitById(
                    id,
                    staffId,
                    RegistrationStatusCode.FINISHED.getCode(),
                    RegistrationStatusCode.UNFINISHED.getCode(),
                    EndAttendanceCode.UNFINISHED.getCode(),
                    RegistrationStatusCode.CANCELED.getCode()
            );
        }
        if (rows == null || rows <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "开始诊疗失败或已开始");
        }
        return Result.success("开始诊疗成功");
    }

    @Override
    public Result<Object> finishVisit(Long id, String token, String scope) {
        if (id == null) return Result.error(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");

        String sc = (scope == null || scope.trim().isEmpty()) ? "self" : scope.trim();
        Integer rows;
        if ("dept".equalsIgnoreCase(sc)) {
            Long deptId = requireDeptId(staffId);
            if (deptId == null) {
                return Result.error(ResultCode.PARAM_ERROR, "无法获取医生科室信息");
            }
            rows = dmsRegistrationMapper.finishVisitByDeptId(
                    id,
                    deptId,
                    EndAttendanceCode.FINISHED.getCode(),
                    RegistrationStatusCode.FINISHED.getCode(),
                    EndAttendanceCode.UNFINISHED.getCode(),
                    RegistrationStatusCode.CANCELED.getCode()
            );
        } else {
            rows = dmsRegistrationMapper.finishVisitById(
                    id,
                    staffId,
                    EndAttendanceCode.FINISHED.getCode(),
                    RegistrationStatusCode.FINISHED.getCode(),
                    EndAttendanceCode.UNFINISHED.getCode(),
                    RegistrationStatusCode.CANCELED.getCode()
            );
        }
        if (rows == null || rows <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "结束就诊失败或已结束");
        }
        return Result.success("结束就诊成功");
    }

    @Override
    public Result<Map<String, Object>> getPatientContext(Long registrationId, String token) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }

        DmsCaseHistory caseHistory = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        List<DmsNonDrugItemRecord> examItems = dmsNonDrugItemRecordMapper.selectByRegistrationIdAndType(registrationId, 1);
        List<DmsNonDrugItemRecord> labItems = dmsNonDrugItemRecordMapper.selectByRegistrationIdAndType(registrationId, 3);

        List<DmsMedicinePrescriptionRecord> medicinePres = dmsMedicinePrescriptionRecordMapper.selectByRegistrationId(registrationId);
        List<Map<String, Object>> medicineWithItems = new ArrayList<>();
        if (medicinePres != null) {
            for (DmsMedicinePrescriptionRecord p : medicinePres) {
                List<DmsMedicineItemRecord> items = dmsMedicineItemRecordMapper.selectByPrescriptionId(p.getId());
                List<Map<String, Object>> itemVos = new ArrayList<>();
                if (items != null) {
                    for (DmsMedicineItemRecord it : items) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", it.getId());
                        m.put("drugId", it.getDrugId());
                        m.put("qty", it.getNum());
                        m.put("usageMeans", it.getUsageMeans());
                        m.put("frequency", it.getFrequency());
                        m.put("medicalAdvice", it.getMedicalAdvice());
                        DmsDrug drug = it.getDrugId() == null ? null : dmsDrugMapper.selectById(it.getDrugId());
                        if (drug != null) {
                            m.put("name", drug.getName());
                            m.put("spec", drug.getFormat());
                            m.put("unitPrice", drug.getPrice());
                        }
                        itemVos.add(m);
                    }
                }
                Map<String, Object> pv = new HashMap<>();
                pv.put("id", p.getId());
                pv.put("amount", p.getAmount());
                pv.put("createTime", p.getCreateTime());
                pv.put("items", itemVos);
                medicineWithItems.add(pv);
            }
        }

        List<DmsHerbalPrescriptionRecord> herbalPres = dmsHerbalPrescriptionRecordMapper.selectByRegistrationId(registrationId);
        List<Map<String, Object>> herbalWithItems = new ArrayList<>();
        if (herbalPres != null) {
            for (DmsHerbalPrescriptionRecord p : herbalPres) {
                List<DmsHerbalItemRecord> items = dmsHerbalItemRecordMapper.selectByPrescriptionId(p.getId());
                Map<String, Object> pv = new HashMap<>();
                pv.put("id", p.getId());
                pv.put("amount", p.getAmount());
                pv.put("therapy", p.getTherapy());
                pv.put("medicalAdvice", p.getMedicalAdvice());
                pv.put("createTime", p.getCreateTime());
                pv.put("items", items == null ? Collections.emptyList() : items);
                herbalWithItems.add(pv);
            }
        }

        BigDecimal examAmount = defaultZero(dmsNonDrugItemRecordMapper.sumAmountByRegistrationId(registrationId));
        BigDecimal medicineAmount = defaultZero(dmsMedicinePrescriptionRecordMapper.sumAmountByRegistrationId(registrationId));
        BigDecimal herbalAmount = defaultZero(dmsHerbalPrescriptionRecordMapper.sumAmountByRegistrationId(registrationId));
        BigDecimal total = examAmount.add(medicineAmount).add(herbalAmount);

        Map<String, Object> billSummary = new HashMap<>();
        billSummary.put("examAmount", scaleMoney(examAmount));
        billSummary.put("medicineAmount", scaleMoney(medicineAmount));
        billSummary.put("herbalAmount", scaleMoney(herbalAmount));
        billSummary.put("totalAmount", scaleMoney(total));

        List<Map<String, Object>> billLines = buildBillLines(examItems, labItems, medicineWithItems, herbalPres);

        Map<String, Object> data = new HashMap<>();
        data.put("caseHistory", caseHistory);
        data.put("examItems", examItems);
        data.put("labItems", labItems);
        data.put("diagnosis", caseHistory == null ? null : caseHistory.getDefiniteDiseStrList());
        data.put("diagnosisBasis", caseHistory == null ? null : caseHistory.getCheckResult());
        data.put("medicinePrescriptions", medicineWithItems);
        data.put("herbalPrescriptions", herbalWithItems);
        data.put("billSummary", billSummary);
        data.put("billLines", billLines);
        return Result.success("查询成功", data);
    }

    @Override
    public Result<List<DmsDrug>> listMedicineDict(String token, String keyword) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        return Result.success("查询成功", dmsDrugMapper.selectEnabledByKeyword(keyword));
    }

    @Override
    public Result<List<DmsNonDrug>> listNonDrugDict(String token, Integer type, String keyword) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (type == null) return Result.error(ResultCode.PARAM_ERROR, "type 不能为空");
        return Result.success("查询成功", dmsNonDrugMapper.selectEnabledByTypeAndKeyword(type, keyword));
    }

    @Override
    @Transactional
    public Result<Object> saveCaseHistory(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }

        DmsCaseHistory ch = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        if (ch == null) {
            ch = new DmsCaseHistory();
            ch.setRegistrationId(registrationId);
            fillPatientBase(ch, registrationId);
            ch.setCreateTime(LocalDateTime.now());
            ch.setStatus(1);
            applyCaseHistoryFields(ch, body);
            dmsCaseHistoryMapper.insert(ch);
        } else {
            applyCaseHistoryFields(ch, body);
            dmsCaseHistoryMapper.updateByRegistrationId(ch);
        }
        return Result.success("保存病历成功");
    }

    @Override
    @Transactional
    public Result<Object> saveDiagnosis(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }

        String diagnosis = asString(body.get("diagnosis"));
        String basis = asString(body.get("basis"));
        DmsCaseHistory ch = dmsCaseHistoryMapper.selectByRegistrationId(registrationId);
        if (ch == null) {
            ch = new DmsCaseHistory();
            ch.setRegistrationId(registrationId);
            fillPatientBase(ch, registrationId);
            ch.setCreateTime(LocalDateTime.now());
            ch.setStatus(1);
            ch.setDefiniteDiseStrList(diagnosis);
            ch.setCheckResult(basis);
            dmsCaseHistoryMapper.insert(ch);
        } else {
            ch.setDefiniteDiseStrList(diagnosis);
            ch.setCheckResult(basis);
            dmsCaseHistoryMapper.updateByRegistrationId(ch);
        }
        return Result.success("保存确诊成功");
    }

    @Override
    @Transactional
    public Result<Object> saveNonDrugItem(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }

        Integer type = asInteger(body.get("type"));
        Long noDrugId = asLong(body.get("noDrugId"));
        if (type == null || noDrugId == null) return Result.error(ResultCode.PARAM_ERROR, "type/noDrugId 不能为空");

        DmsNonDrug dict = dmsNonDrugMapper.selectById(noDrugId);
        if (dict == null || dict.getStatus() == null || dict.getStatus() != 1) {
            return Result.error(ResultCode.PARAM_ERROR, "无效的非药品项目");
        }
        if (dict.getRecordType() == null || !dict.getRecordType().equals(type)) {
            return Result.error(ResultCode.PARAM_ERROR, "项目类型不匹配");
        }

        DmsNonDrugItemRecord rec = new DmsNonDrugItemRecord();
        rec.setRegistrationId(registrationId);
        rec.setStatus(0);
        rec.setType(type);
        rec.setNoDrugId(noDrugId);
        rec.setAim(asString(body.get("aim")));
        rec.setDemand(asString(body.get("remark")));
        rec.setCreateStaffId(staffId);
        rec.setCreateTime(LocalDateTime.now());
        rec.setAmount(dict.getPrice());
        dmsNonDrugItemRecordMapper.insert(rec);
        bmsCashierService.createNonDrugPayable(
                registrationId, rec.getId(), dict.getPrice(), dict.getName());
        return Result.success("保存成功");
    }

    @Override
    @Transactional
    public Result<Object> saveMedicinePrescription(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }
        Object itemsObj = body.get("items");
        if (!(itemsObj instanceof List)) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        List<?> rawItems = (List<?>) itemsObj;
        if (rawItems.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");

        BigDecimal total = BigDecimal.ZERO;
        List<DmsMedicineItemRecord> itemRecords = new ArrayList<>();
        for (Object o : rawItems) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            Long drugId = asLong(m.get("drugId"));
            Long qty = asLong(m.get("qty"));
            if (drugId == null || qty == null || qty <= 0) {
                return Result.error(ResultCode.PARAM_ERROR, "drugId/qty 无效");
            }
            DmsDrug drug = dmsDrugMapper.selectById(drugId);
            if (drug == null || drug.getStatus() == null || drug.getStatus() != 1) {
                return Result.error(ResultCode.PARAM_ERROR, "无效药品");
            }
            BigDecimal line = defaultZero(drug.getPrice()).multiply(BigDecimal.valueOf(qty));
            total = total.add(line);

            DmsMedicineItemRecord it = new DmsMedicineItemRecord();
            it.setDrugId(drugId);
            it.setStatus(0);
            it.setMedicineUsage(asInteger(m.get("medicineUsage")) == null ? 1 : asInteger(m.get("medicineUsage")));
            it.setFrequency(asInteger(m.get("frequency")));
            it.setDays(asLong(m.get("days")));
            it.setNum(qty);
            it.setMedicalAdvice(asString(m.get("medicalAdvice")));
            it.setUsageNum(asLong(m.get("usageNum")));
            it.setUsageMeans(asInteger(m.get("usageMeans")));
            it.setUsageNumUnit(asInteger(m.get("usageNumUnit")));
            it.setCurrentNum(qty);
            itemRecords.add(it);
        }

        DmsMedicinePrescriptionRecord pres = new DmsMedicinePrescriptionRecord();
        pres.setStatus(1);
        pres.setCreateTime(LocalDateTime.now());
        pres.setAmount(total);
        pres.setName(asString(body.get("name")));
        pres.setRegistrationId(registrationId);
        pres.setRefundStatus(0L);
        pres.setType(1);
        pres.setCreateStaffId(staffId);
        dmsMedicinePrescriptionRecordMapper.insert(pres);

        for (DmsMedicineItemRecord it : itemRecords) {
            it.setPrescriptionId(pres.getId());
        }
        dmsMedicineItemRecordMapper.insertBatch(itemRecords);
        bmsCashierService.createMedicinePrescriptionPayable(registrationId, pres.getId(), total);
        return Result.success("保存处方成功");
    }

    @Override
    @Transactional
    public Result<Object> saveHerbalPrescription(Long registrationId, String token, Map<String, Object> body) {
        Long staffId = requireStaffId(token);
        if (staffId == null) return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        if (!canOperateRegistration(staffId, registrationId)) {
            return Result.error(ResultCode.PERMISSION_DENIED, "无权限访问该就诊记录");
        }
        Object itemsObj = body.get("items");
        if (!(itemsObj instanceof List)) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");
        List<?> rawItems = (List<?>) itemsObj;
        if (rawItems.isEmpty()) return Result.error(ResultCode.PARAM_ERROR, "items 不能为空");

        BigDecimal total = BigDecimal.ZERO;
        List<DmsHerbalItemRecord> itemRecords = new ArrayList<>();
        for (Object o : rawItems) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            Long drugId = asLong(m.get("drugId"));
            Long totalNum = asLong(m.get("totalNum"));
            if (drugId == null || totalNum == null || totalNum <= 0) {
                return Result.error(ResultCode.PARAM_ERROR, "drugId/totalNum 无效");
            }
            DmsDrug drug = dmsDrugMapper.selectById(drugId);
            if (drug == null || drug.getStatus() == null || drug.getStatus() != 1) {
                return Result.error(ResultCode.PARAM_ERROR, "无效药品");
            }
            total = total.add(defaultZero(drug.getPrice()).multiply(BigDecimal.valueOf(totalNum)));

            DmsHerbalItemRecord it = new DmsHerbalItemRecord();
            it.setStatus(0);
            it.setDrugId(drugId);
            it.setMedicalAdvice(asString(m.get("medicalAdvice")));
            it.setFootnote(asString(m.get("footnote")));
            it.setUsageNum(asLong(m.get("usageNum")));
            it.setUsageNumUnit(asInteger(m.get("usageNumUnit")));
            it.setTotalNum(totalNum);
            it.setCurrentNum(totalNum);
            itemRecords.add(it);
        }

        DmsHerbalPrescriptionRecord pres = new DmsHerbalPrescriptionRecord();
        pres.setStatus(1);
        pres.setCreateTime(LocalDateTime.now());
        pres.setAmount(total);
        pres.setName(asString(body.get("name")));
        pres.setTherapy(asString(body.get("therapy")));
        pres.setTherapyDetails(asString(body.get("therapyDetails")));
        pres.setMedicalAdvice(asString(body.get("medicalAdvice")));
        pres.setPairNum(asLong(body.get("pairNum")));
        pres.setRegistrationId(registrationId);
        pres.setFrequency(asInteger(body.get("frequency")));
        pres.setUsageMeans(asInteger(body.get("usageMeans")));
        pres.setType(2);
        pres.setCreateStaffId(staffId);
        dmsHerbalPrescriptionRecordMapper.insert(pres);

        for (DmsHerbalItemRecord it : itemRecords) {
            it.setPrescriptionId(pres.getId());
        }
        dmsHerbalItemRecordMapper.insertBatch(itemRecords);
        bmsCashierService.createHerbalPrescriptionPayable(registrationId, pres.getId(), total);
        return Result.success("保存草药处方成功");
    }

    private Long requireStaffId(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        return jwtUtil.getUserIdFromToken(token);
    }

    private Long requireDeptId(Long staffId) {
        if (staffId == null) {
            return null;
        }
        try {
            Integer deptId = smsStaffMapper.selectDeptIdById(staffId);
            return deptId == null ? null : deptId.longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean canOperateRegistration(Long staffId, Long registrationId) {
        if (staffId == null || registrationId == null) return false;
        try {
            Integer cnt = dmsRegistrationMapper.countOperatePermission(registrationId, staffId);
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void fillPatientBase(DmsCaseHistory ch, Long registrationId) {
        try {
            Map<String, Object> row = dmsRegistrationMapper.selectPatientBaseByRegistrationId(registrationId);
            ch.setPatientId(asLong(row.get("patientId")));
            ch.setName(asString(row.get("name")));
            Integer g = asInteger(row.get("gender"));
            ch.setGender(g);
            Integer age = asInteger(row.get("age"));
            if (age != null) ch.setAgeStr(age + "岁");
            ch.setStartDate(LocalDateTime.now());
        } catch (Exception ignored) {
        }
    }

    private void applyCaseHistoryFields(DmsCaseHistory ch, Map<String, Object> body) {
        if (body == null) return;
        ch.setChiefComplaint(asString(body.get("chiefComplaint")));
        ch.setHistoryOfPresentIllness(asString(body.get("historyOfPresentIllness")));
        ch.setHistoryOfTreatment(asString(body.get("historyOfTreatment")));
        ch.setPastHistory(asString(body.get("pastHistory")));
        ch.setAllergies(asString(body.get("allergies")));
        ch.setHealthCheckup(asString(body.get("healthCheckup")));
        ch.setStartDate(parseDateTime(body.get("startDate")));
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer asInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try {
            if (s.length() == 10) return LocalDate.parse(s).atStartOfDay();
            if (s.contains("T")) {
                if (s.length() >= 19) s = s.substring(0, 19);
                return LocalDateTime.parse(s.replace(" ", "T"));
            }
            return LocalDateTime.parse(s.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scaleMoney(BigDecimal v) {
        return defaultZeroStatic(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal defaultZeroStatic(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 患者账单明细行（检查/检验/成药/草药），金额与处方、非药品开单一致。
     */
    private List<Map<String, Object>> buildBillLines(
            List<DmsNonDrugItemRecord> examItems,
            List<DmsNonDrugItemRecord> labItems,
            List<Map<String, Object>> medicineWithItems,
            List<DmsHerbalPrescriptionRecord> herbalPres) {
        List<Map<String, Object>> lines = new ArrayList<>();
        appendNonDrugBillLines(lines, examItems, "检查");
        appendNonDrugBillLines(lines, labItems, "检验");
        if (medicineWithItems != null) {
            for (Map<String, Object> pv : medicineWithItems) {
                Object itemsObj = pv.get("items");
                if (!(itemsObj instanceof List)) {
                    continue;
                }
                for (Object o : (List<?>) itemsObj) {
                    if (!(o instanceof Map)) {
                        continue;
                    }
                    Map<?, ?> it = (Map<?, ?>) o;
                    String name = asString(it.get("name"));
                    if (name == null || name.isEmpty()) {
                        name = "药品";
                    }
                    Long qtyBox = asLong(it.get("qty"));
                    long qty = qtyBox == null ? 0L : qtyBox;
                    BigDecimal unit = toBigDecimal(it.get("unitPrice"));
                    BigDecimal lineAmt = unit.multiply(BigDecimal.valueOf(qty));
                    Map<String, Object> line = new LinkedHashMap<>();
                    line.put("category", "成药");
                    line.put("itemName", name);
                    line.put("quantity", qty);
                    line.put("unitPrice", scaleMoney(unit));
                    line.put("amount", scaleMoney(lineAmt));
                    lines.add(line);
                }
            }
        }
        if (herbalPres != null) {
            for (DmsHerbalPrescriptionRecord p : herbalPres) {
                List<DmsHerbalItemRecord> items = dmsHerbalItemRecordMapper.selectByPrescriptionId(p.getId());
                if (items == null) {
                    continue;
                }
                for (DmsHerbalItemRecord it : items) {
                    if (it.getStatus() != null && it.getStatus() == 2) {
                        continue;
                    }
                    DmsDrug drug = it.getDrugId() == null ? null : dmsDrugMapper.selectById(it.getDrugId());
                    if (drug == null) {
                        continue;
                    }
                    long tn = it.getTotalNum() == null ? 0L : it.getTotalNum();
                    BigDecimal unit = defaultZero(drug.getPrice());
                    BigDecimal lineAmt = unit.multiply(BigDecimal.valueOf(tn));
                    Map<String, Object> line = new LinkedHashMap<>();
                    line.put("category", "草药");
                    line.put("itemName", drug.getName());
                    line.put("quantity", tn);
                    line.put("unitPrice", scaleMoney(unit));
                    line.put("amount", scaleMoney(lineAmt));
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private void appendNonDrugBillLines(List<Map<String, Object>> lines,
                                        List<DmsNonDrugItemRecord> records,
                                        String category) {
        if (records == null) {
            return;
        }
        for (DmsNonDrugItemRecord r : records) {
            if (r.getStatus() != null && r.getStatus() == 2) {
                continue;
            }
            DmsNonDrug nd = r.getNoDrugId() == null ? null : dmsNonDrugMapper.selectById(r.getNoDrugId());
            String name = nd != null && nd.getName() != null ? nd.getName() : "项目";
            BigDecimal amt = defaultZero(r.getAmount());
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("category", category);
            line.put("itemName", name);
            line.put("quantity", 1);
            line.put("unitPrice", scaleMoney(amt));
            line.put("amount", scaleMoney(amt));
            lines.add(line);
        }
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        if (o instanceof Number) {
            return BigDecimal.valueOf(((Number) o).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(o).trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer parseNoon(String session) {
        if (session == null || session.trim().isEmpty()) return null;
        if ("上午".equals(session)) return NoonCode.MORNING.getCode();
        if ("下午".equals(session)) return NoonCode.AFTERNOON.getCode();
        return null;
    }
}

