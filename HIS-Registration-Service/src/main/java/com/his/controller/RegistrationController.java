package com.his.controller;

import com.his.domain.Result;
import com.his.dto.PageQueryDto;
import com.his.dto.PayItemsRequestDto;
import com.his.dto.RegistrationDto;
import com.his.service.IRegistrationService;
import com.his.vo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    private final IRegistrationService registrationService;
    public RegistrationController(IRegistrationService registrationService) { this.registrationService = registrationService; }

    @GetMapping("/registration-ranks")
    public Result<List<RegistrationRankVo>> listRegistrationRanks() {
        return registrationService.listRegistrationRanks();
    }

    @GetMapping("/departments")
    public Result<List<DeptVo>> getDepartments() {
        return registrationService.listDepartments();
    }

    @GetMapping("/departments/{deptId}/doctors")
    public Result<List<DoctorVo>> getDoctorsByDept(@PathVariable("deptId") Long deptId) {
        return registrationService.listDoctorsByDept(deptId);
    }

    @GetMapping("/doctors/available")
    public Result<List<DoctorVo>> listAvailableDoctors(@RequestParam("deptId") Long deptId, @RequestParam("date") String date, @RequestParam(value = "session", required = false) String session) {
        return registrationService.listAvailableDoctors(deptId, date, session);
    }

    @GetMapping("/patient")
    public Result<Map<String, Object>> getPatientByIdentificationNo(@RequestParam("identificationNo") Long identificationNo) {
        return registrationService.getPatientByIdentificationNo(identificationNo);
    }

    @PostMapping("/registrations")
    public Result<RegistrationCreateResponseVo> createRegistration(@RequestBody RegistrationDto body) {
        return registrationService.createRegistration(body);
    }

    @GetMapping("/registrations/{id}/payables")
    public Result<List<PayableItemVo>> listPayables(@PathVariable("id") Long id) {
        return registrationService.listPayables(id);
    }

    @PostMapping("/registrations/{id}/pay-items")
    public Result<PaymentResponseVo> paySelectedItems(@PathVariable("id") Long id, @RequestBody(required = false) PayItemsRequestDto body) {
        return registrationService.paySelectedItems(id, body);
    }

    @PutMapping("/registrations/{id}")
    public Result<Object> updateRegistration(@PathVariable("id") Long id, @RequestBody RegistrationDto body) {
        return registrationService.updateRegistration(id, body);
    }

    @DeleteMapping("/registrations/{id}")
    public Result<Object> deleteRegistration(@PathVariable("id") Long id) { return registrationService.deleteRegistration(id); }
    @PostMapping("/registrations/{id}/cancel")
    public Result<Object> cancelRegistration(@PathVariable("id") Long id) { return registrationService.cancelRegistration(id); }

    @PostMapping("/getAllByPage")
    public Result<Map<String, Object>> getAllByPage(@RequestBody PageQueryDto body) {
        return registrationService.getAllByPage(body);
    }

    @PostMapping("/registrations/{id}/pay")
    public Result<PaymentResponseVo> payRegistration(@PathVariable("id") Long id) { return registrationService.payRegistration(id); }
    @PostMapping("/registrations/{id}/refund")
    public Result<Object> refundRegistration(@PathVariable("id") Long id) { return registrationService.refundRegistration(id); }
}
