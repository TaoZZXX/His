package com.his.controller;

import com.his.domain.DmsDrug;
import com.his.domain.DmsNonDrug;
import com.his.domain.Result;
import com.his.service.IDoctorDeskService;
import com.his.vo.OutpatientDeskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor/desk")
public class DoctorDeskController {

    @Autowired
    private IDoctorDeskService doctorDeskService;

    @GetMapping("/patients")
    public Result<OutpatientDeskVo> listPatients(
            @RequestParam("token") String token,
            @RequestParam(defaultValue = "self") String scope,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String session
    ) {
        return doctorDeskService.listPatients(token, scope, keyword, date, session);
    }

    @PostMapping("/patients/{id}/start")
    public Result<Object> startVisit(
            @PathVariable("id") Long id,
            @RequestParam("token") String token,
            @RequestParam(defaultValue = "self") String scope) {
        return doctorDeskService.startVisit(id, token, scope);
    }

    @PostMapping("/patients/{id}/finish")
    public Result<Object> finishVisit(
            @PathVariable("id") Long id,
            @RequestParam("token") String token,
            @RequestParam(defaultValue = "self") String scope) {
        return doctorDeskService.finishVisit(id, token, scope);
    }

    @GetMapping("/patients/{id}/context")
    public Result<Map<String, Object>> getPatientContext(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token
    ) {
        return doctorDeskService.getPatientContext(registrationId, token);
    }

    @GetMapping("/dict/medicines")
    public Result<List<DmsDrug>> listMedicineDict(
            @RequestParam("token") String token,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return doctorDeskService.listMedicineDict(token, keyword);
    }

    @GetMapping("/dict/non-drug")
    public Result<List<DmsNonDrug>> listNonDrugDict(
            @RequestParam("token") String token,
            @RequestParam("type") Integer type,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return doctorDeskService.listNonDrugDict(token, type, keyword);
    }

    @PostMapping("/patients/{id}/case-history")
    public Result<Object> saveCaseHistory(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        return doctorDeskService.saveCaseHistory(registrationId, token, body);
    }

    @PostMapping("/patients/{id}/diagnosis")
    public Result<Object> saveDiagnosis(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        return doctorDeskService.saveDiagnosis(registrationId, token, body);
    }

    @PostMapping("/patients/{id}/non-drug-items")
    public Result<Object> saveNonDrugItem(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        return doctorDeskService.saveNonDrugItem(registrationId, token, body);
    }

    @PostMapping("/patients/{id}/prescriptions/medicine")
    public Result<Object> saveMedicinePrescription(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        return doctorDeskService.saveMedicinePrescription(registrationId, token, body);
    }

    @PostMapping("/patients/{id}/prescriptions/herbal")
    public Result<Object> saveHerbalPrescription(
            @PathVariable("id") Long registrationId,
            @RequestParam("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        return doctorDeskService.saveHerbalPrescription(registrationId, token, body);
    }
}

