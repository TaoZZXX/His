package com.his.controller;

import com.his.domain.Result;
import com.his.service.IPharmacyDeskService;
import com.his.vo.PharmacyMedicineLineVo;
import com.his.vo.PharmacyPatientQueueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyDeskController {
    private final IPharmacyDeskService pharmacyDeskService;
    public PharmacyDeskController(IPharmacyDeskService pharmacyDeskService) { this.pharmacyDeskService = pharmacyDeskService; }

    @GetMapping("/patients")
    public Result<List<PharmacyPatientQueueVo>> patients(@RequestParam("token") String token,
                                                         @RequestParam(value = "mode", defaultValue = "pending") String mode,
                                                         @RequestParam(value = "keyword", required = false) String keyword,
                                                         @RequestParam(value = "limit", required = false) Integer limit) {
        return pharmacyDeskService.listPatients(token, mode, keyword, limit);
    }

    @GetMapping("/registrations/{registrationId}/medicine-lines")
    public Result<List<PharmacyMedicineLineVo>> medicineLines(@RequestParam("token") String token, @PathVariable("registrationId") Long registrationId) {
        return pharmacyDeskService.listMedicineLines(token, registrationId);
    }

    @PostMapping("/items/dispense")
    public Result<Object> dispense(@RequestParam("token") String token, @RequestBody(required = false) Map<String, Object> body) {
        return pharmacyDeskService.dispenseItems(token, body);
    }

    @PostMapping("/items/refund")
    public Result<Object> refund(@RequestParam("token") String token, @RequestBody(required = false) Map<String, Object> body) {
        return pharmacyDeskService.refundItems(token, body);
    }
}
