package com.his.service;

import com.his.domain.DmsDrug;
import com.his.domain.DmsNonDrug;
import com.his.domain.Result;
import com.his.vo.OutpatientDeskVo;

import java.util.List;
import java.util.Map;

public interface IDoctorDeskService {
    Result<OutpatientDeskVo> listPatients(String token, String scope, String keyword, String date, String session);

    /**
     * @param scope self=仅本人排班挂号；dept=同科室挂号（与队列「科室」视图一致）
     */
    Result<Object> startVisit(Long id, String token, String scope);

    Result<Object> finishVisit(Long id, String token, String scope);

    Result<Map<String, Object>> getPatientContext(Long registrationId, String token);

    Result<List<DmsDrug>> listMedicineDict(String token, String keyword);

    Result<List<DmsNonDrug>> listNonDrugDict(String token, Integer type, String keyword);

    Result<Object> saveCaseHistory(Long registrationId, String token, Map<String, Object> body);

    Result<Object> saveDiagnosis(Long registrationId, String token, Map<String, Object> body);

    Result<Object> saveNonDrugItem(Long registrationId, String token, Map<String, Object> body);

    Result<Object> saveMedicinePrescription(Long registrationId, String token, Map<String, Object> body);

    Result<Object> saveHerbalPrescription(Long registrationId, String token, Map<String, Object> body);
}

