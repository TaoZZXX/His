package com.his.service;

import com.his.domain.Result;
import com.his.vo.PharmacyMedicineLineVo;
import com.his.vo.PharmacyPatientQueueVo;

import java.util.List;
import java.util.Map;

public interface IPharmacyDeskService {

    Result<List<PharmacyPatientQueueVo>> listPatients(String token, String mode, String keyword, Integer limit);

    Result<List<PharmacyMedicineLineVo>> listMedicineLines(String token, Long registrationId);

    Result<Object> dispenseItems(String token, Map<String, Object> body);

    Result<Object> refundItems(String token, Map<String, Object> body);
}
