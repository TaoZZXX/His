package com.his.service.impl;

import com.his.domain.Result;
import com.his.dto.PageQueryDto;
import com.his.dto.PayItemsRequestDto;
import com.his.dto.RegistrationDto;
import com.his.enums.EndAttendanceCode;
import com.his.enums.GenderCode;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.mapper.RegistrationMapper;
import com.his.service.IRegistrationService;
import com.his.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RegistrationService implements IRegistrationService {
    private final RegistrationMapper registrationMapper;
    public RegistrationService(RegistrationMapper registrationMapper) { this.registrationMapper = registrationMapper; }

    @Override public Result<List<RegistrationRankVo>> listRegistrationRanks() { return Result.success("查询成功", registrationMapper.selectRegistrationRanks()); }
    @Override public Result<List<DeptVo>> listDepartments() { return Result.success("获取科室列表成功", registrationMapper.selectDepartments()); }
    @Override public Result<List<DoctorVo>> listDoctorsByDept(Long deptId) { return Result.success("获取医生列表成功", registrationMapper.selectDoctorsByDept(deptId)); }

    @Override
    public Result<List<DoctorVo>> listAvailableDoctors(Long deptId, String date, String session) {
        Integer noon = "上午".equals(session) ? 0 : ("下午".equals(session) ? 1 : null);
        LocalDate d = LocalDate.parse(date);
        return Result.success("查询成功", registrationMapper.selectAvailableDoctors(deptId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), noon));
    }

    @Override
    public Result<Map<String, Object>> getPatientByIdentificationNo(Long identificationNo) {
        List<Map<String, Object>> rows = registrationMapper.selectPatientByIdentificationNo(identificationNo);
        return Result.success("查询成功", rows.isEmpty() ? null : rows.get(0));
    }

    @Override
    @Transactional
    public Result<RegistrationCreateResponseVo> createRegistration(RegistrationDto body) {
        if (body == null) return paramError("挂号参数不能为空");
        Long identificationNo = body.getIdentificationNo();
        if (identificationNo == null) return paramError("身份证号不能为空");
        Map<String, Object> patient = ensurePatient(body);
        Long deptId = body.getDepartmentId();
        Long doctorId = body.getDoctorId();
        String session = body.getSession();
        LocalDate attendanceDate = body.getRegistrationDate();
        if (deptId == null || doctorId == null || attendanceDate == null) return paramError("挂号科室/医生/日期不能为空");
        Long skdId = resolveSkdId(doctorId, deptId, attendanceDate, session);
        if (skdId == null) return paramError("未找到匹配的医生排班");
        Map<String, Object> reg = new HashMap<>();
        reg.put("patientId", ((Number) patient.get("id")).longValue());
        reg.put("createTime", LocalDate.now());
        reg.put("endAttendance", EndAttendanceCode.UNFINISHED.getCode());
        reg.put("status", RegistrationStatusCode.UNFINISHED.getCode());
        reg.put("skdId", skdId);
        reg.put("needBook", "是".equals(body.getMedicalRecord()) ? 1 : 0);
        reg.put("bindStatus", 0);
        reg.put("deptId", deptId);
        reg.put("attendanceDate", attendanceDate);
        reg.put("patientAgeStr", null);
        registrationMapper.insertRegistration(reg);
        Long regId = ((Number) reg.get("id")).longValue();
        BigDecimal fee = resolveRegistrationFee(body.getRankId(), body.getAmount());
        Map<String, Object> payable = new HashMap<>();
        payable.put("registrationId", regId); payable.put("itemType", 4); payable.put("sourceId", regId); payable.put("itemName", "挂号费");
        payable.put("amount", fee); payable.put("paidAmount", BigDecimal.ZERO); payable.put("status", 0); payable.put("createTime", LocalDateTime.now()); payable.put("updateTime", LocalDateTime.now());
        registrationMapper.insertPayable(payable);
        return Result.success("挂号成功", new RegistrationCreateResponseVo(regId));
    }

    @Override public Result<List<PayableItemVo>> listPayables(Long registrationId) { return Result.success("查询成功", registrationMapper.selectPayables(registrationId)); }

    @Override
    @Transactional
    public Result<PaymentResponseVo> paySelectedItems(Long registrationId, PayItemsRequestDto body) {
        if (registrationId == null) return paramError("挂号ID不能为空");
        List<Long> ids = body == null || body.getPayableItemIds() == null ? new ArrayList<>() : body.getPayableItemIds();
        if (ids.isEmpty()) ids = registrationMapper.selectUnpaidPayableIds(registrationId);
        if (ids.isEmpty()) return Result.success("无需缴费", new PaymentResponseVo(null));
        for (Long id : ids) registrationMapper.markPayablePaid(id);
        Integer unpaid = registrationMapper.countUnpaidPayables(registrationId);
        registrationMapper.updateRegistrationBindStatus(registrationId, unpaid != null && unpaid == 0 ? 1 : 0);
        return Result.success("缴费成功", new PaymentResponseVo(System.currentTimeMillis() % 100000000L));
    }

    @Override
    public Result<Object> updateRegistration(Long id, RegistrationDto body) {
        if (id == null || body == null) return paramError("更新参数不能为空");
        Long deptId = body.getDepartmentId();
        Long doctorId = body.getDoctorId();
        LocalDate attendanceDate = body.getRegistrationDate();
        if (deptId == null || doctorId == null || attendanceDate == null) return paramError("挂号科室/医生/日期不能为空");
        Long skdId = resolveSkdId(doctorId, deptId, attendanceDate, body.getSession());
        if (skdId == null) return paramError("未找到匹配的医生排班");
        registrationMapper.updateRegistrationBasic(id, deptId, skdId, attendanceDate);
        return Result.success("更新挂号信息成功");
    }

    @Override public Result<Object> deleteRegistration(Long id) { registrationMapper.deleteRegistration(id); return Result.success("删除挂号记录成功"); }
    @Override public Result<Object> cancelRegistration(Long id) { registrationMapper.cancelRegistration(id, RegistrationStatusCode.CANCELED.getCode(), EndAttendanceCode.FINISHED.getCode()); return Result.success("退号成功"); }

    @Override
    public Result<Map<String, Object>> getAllByPage(PageQueryDto body) {
        int page = body == null || body.getPage() == null ? 0 : body.getPage();
        int size = body == null || body.getSize() == null ? 10 : body.getSize();
        int offset = Math.max(page, 0) * Math.max(size, 1);
        String kw = body == null ? null : body.getKeyword(); if (kw != null && kw.trim().isEmpty()) kw = null;
        List<RegistrationPageVo> records = registrationMapper.selectRegistrationPage(kw, size, offset);
        long total = registrationMapper.countRegistrationPage(kw);
        Map<String, Object> data = new HashMap<>(); data.put("records", records); data.put("total", total); data.put("page", page); data.put("size", size);
        return Result.success("查询挂号记录成功", data);
    }

    @Override public Result<PaymentResponseVo> payRegistration(Long id) { return paySelectedItems(id, null); }
    @Override @Transactional public Result<Object> refundRegistration(Long id) { registrationMapper.resetPayables(id); registrationMapper.updateRegistrationBindStatus(id, 0); return Result.success("退费成功"); }

    private Map<String, Object> ensurePatient(RegistrationDto body) {
        Long identificationNo = body.getIdentificationNo();
        List<Map<String, Object>> rows = registrationMapper.selectPatientByIdentificationNo(identificationNo);
        if (!rows.isEmpty()) return rows.get(0);
        Map<String, Object> row = new HashMap<>();
        row.put("name", body.getName());
        row.put("dateOfBirth", body.getBirthDate());
        row.put("identificationNo", identificationNo);
        row.put("homeAddress", body.getAddress());
        row.put("phoneNo", body.getContact());
        row.put("gender", "男".equals(body.getGender()) || GenderCode.MALE.getDescription().equals(body.getGender()) ? GenderCode.MALE.getCode() : GenderCode.FEMALE.getCode());
        row.put("medicalRecordNo", body.getMedicalRecord());
        registrationMapper.insertPatient(row);
        return registrationMapper.selectPatientById(((Number) row.get("id")).longValue());
    }

    private Long resolveSkdId(Long doctorId, Long deptId, LocalDate attendanceDate, String session) {
        Integer noon = "上午".equals(session) ? 0 : ("下午".equals(session) ? 1 : null);
        List<Long> ids = registrationMapper.selectSkdIds(doctorId, deptId, attendanceDate.atStartOfDay(), attendanceDate.plusDays(1).atStartOfDay(), noon);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private BigDecimal resolveRegistrationFee(Long rankId, Object amount) {
        if (rankId != null) {
            BigDecimal p = registrationMapper.selectRankPrice(rankId);
            if (p != null) return p;
        }
        if (amount instanceof Number) return BigDecimal.valueOf(((Number) amount).doubleValue());
        try { return new BigDecimal(String.valueOf(amount)); } catch (Exception e) { return BigDecimal.ZERO; }
    }
    private static String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private static Long asLong(Object o) { if (o == null) return null; if (o instanceof Number) return ((Number) o).longValue(); try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; } }
    private <T> Result<T> paramError(String message) { return Result.error(ResultCode.PARAM_ERROR, message); }
}
