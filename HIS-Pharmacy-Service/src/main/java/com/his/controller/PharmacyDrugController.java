package com.his.controller;

import com.his.domain.DmsDrug;
import com.his.domain.Result;
import com.his.service.IPharmacyDrugService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pharmacy/drugs")
public class PharmacyDrugController {
    private final IPharmacyDrugService pharmacyDrugService;
    public PharmacyDrugController(IPharmacyDrugService pharmacyDrugService) { this.pharmacyDrugService = pharmacyDrugService; }

    @GetMapping
    public Result<Map<String, Object>> page(@RequestParam("token") String token,
                                            @RequestParam(value = "page", defaultValue = "1") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "status", required = false) Integer status) {
        return pharmacyDrugService.page(token, page, size, keyword, status);
    }

    @PostMapping
    public Result<Object> create(@RequestParam("token") String token, @RequestBody DmsDrug drug) {
        return pharmacyDrugService.create(token, drug);
    }

    @PutMapping("/{id}")
    public Result<Object> update(@RequestParam("token") String token, @PathVariable("id") Long id, @RequestBody DmsDrug drug) {
        return pharmacyDrugService.update(token, id, drug);
    }
}
