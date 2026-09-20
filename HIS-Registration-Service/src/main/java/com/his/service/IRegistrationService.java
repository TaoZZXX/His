package com.his.service;

import com.his.domain.Result;
import com.his.dto.PageQueryDto;
import com.his.dto.PayItemsRequestDto;
import com.his.dto.RegistrationDto;
import com.his.vo.*;

import java.util.List;
import java.util.Map;

public interface IRegistrationService {
    Result<List<RegistrationRankVo>> listRegistrationRanks();
    Result<List<DeptVo>> listDepartments();
    Result<List<DoctorVo>> listDoctorsByDept(Long deptId);
    Result<List<DoctorVo>> listAvailableDoctors(Long deptId, String date, String session);
    Result<Map<String, Object>> getPatientByIdentificationNo(Long identificationNo);
    Result<RegistrationCreateResponseVo> createRegistration(RegistrationDto body);
    Result<List<PayableItemVo>> listPayables(Long registrationId);
    Result<PaymentResponseVo> paySelectedItems(Long registrationId, PayItemsRequestDto body);
    Result<Object> updateRegistration(Long id, RegistrationDto body);
    Result<Object> deleteRegistration(Long id);
    Result<Object> cancelRegistration(Long id);
    Result<Map<String, Object>> getAllByPage(PageQueryDto body);
    Result<PaymentResponseVo> payRegistration(Long id);
    Result<Object> refundRegistration(Long id);
}
