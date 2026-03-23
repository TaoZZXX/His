package com.his.controller;

import com.his.domain.Result;
import com.his.service.IPharmacyDeskService;
import com.his.vo.PharmacyMedicineLineVo;
import com.his.vo.PharmacyPatientQueueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 药房工作台：待发药 / 已发药、确认发药、退药（西药处方明细）
 */
@RestController
@RequestMapping("/pharmacy")
public class PharmacyDeskController {

    @Autowired
    private IPharmacyDeskService pharmacyDeskService;

    /**
     * @param mode pending=未发药队列；dispensed=已发药队列（无不待发明细）
     */
    @GetMapping("/patients")
    public Result<List<PharmacyPatientQueueVo>> patients(
            @RequestParam("token") String token,
            @RequestParam(value = "mode", defaultValue = "pending") String mode,
            @RequestParam(value = "keyword", required = false) String keyword,
            /** 不传时：无关键词默认最近 80 条，有关键词最多 300 条；最大 500 */
            @RequestParam(value = "limit", required = false) Integer limit) {
        return pharmacyDeskService.listPatients(token, mode, keyword, limit);
    }

    @GetMapping("/registrations/{registrationId}/medicine-lines")
    public Result<List<PharmacyMedicineLineVo>> medicineLines(
            @RequestParam("token") String token,
            @PathVariable("registrationId") Long registrationId) {
        return pharmacyDeskService.listMedicineLines(token, registrationId);
    }

    @PostMapping("/items/dispense")
    public Result<Object> dispense(
            @RequestParam("token") String token,
            @RequestBody(required = false) Map<String, Object> body) {
        return pharmacyDeskService.dispenseItems(token, body);
    }

    @PostMapping("/items/refund")
    public Result<Object> refund(
            @RequestParam("token") String token,
            @RequestBody(required = false) Map<String, Object> body) {
        return pharmacyDeskService.refundItems(token, body);
    }
}
