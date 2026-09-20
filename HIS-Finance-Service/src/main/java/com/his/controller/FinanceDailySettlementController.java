package com.his.controller;

import com.his.domain.BmsDailySettlement;
import com.his.domain.Result;
import com.his.service.IBmsDailySettlementService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance/daily-settlement")
public class FinanceDailySettlementController {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final IBmsDailySettlementService bmsDailySettlementService;
    public FinanceDailySettlementController(IBmsDailySettlementService bmsDailySettlementService) { this.bmsDailySettlementService = bmsDailySettlementService; }

    @GetMapping("/list")
    public Result<List<BmsDailySettlement>> list(@RequestParam("token") String token, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        LocalDateTime qs = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime qe = LocalDate.parse(endDate).atTime(23, 59, 59);
        return Result.success("ok", bmsDailySettlementService.listByReportRange(token, qs, qe));
    }

    @GetMapping("/{id}")
    public Result<BmsDailySettlement> getOne(@PathVariable("id") Long id, @RequestParam("token") String token) {
        return Result.success("ok", bmsDailySettlementService.getById(token, id));
    }

    @PostMapping("/generate")
    public Result<BmsDailySettlement> generate(@RequestParam("token") String token, @RequestBody Map<String, String> body) {
        LocalDateTime rangeStart = LocalDateTime.parse(body.get("rangeStart").trim(), DT);
        LocalDateTime rangeEnd = LocalDateTime.parse(body.get("rangeEnd").trim(), DT);
        return Result.success("生成成功", bmsDailySettlementService.generate(token, rangeStart, rangeEnd));
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable("id") Long id, @RequestParam("token") String token) {
        bmsDailySettlementService.audit(token, id);
        return Result.success("核对成功");
    }
}
