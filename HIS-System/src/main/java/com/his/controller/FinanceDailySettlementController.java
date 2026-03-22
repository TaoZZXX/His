package com.his.controller;

import com.his.domain.BmsDailySettlement;
import com.his.domain.Result;
import com.his.service.IBmsDailySettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance/daily-settlement")
public class FinanceDailySettlementController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private IBmsDailySettlementService bmsDailySettlementService;

    /**
     * 按报表生成时间查询日结列表（用于历史日结 / 核对左侧列表）
     */
    @GetMapping("/list")
    public Result<List<BmsDailySettlement>> list(
            @RequestParam("token") String token,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate d0 = LocalDate.parse(startDate);
        LocalDate d1 = LocalDate.parse(endDate);
        LocalDateTime qs = d0.atStartOfDay();
        LocalDateTime qe = d1.atTime(23, 59, 59);
        return Result.success("ok", bmsDailySettlementService.listByReportRange(token, qs, qe));
    }

    @GetMapping("/{id}")
    public Result<BmsDailySettlement> getOne(
            @PathVariable("id") Long id,
            @RequestParam("token") String token) {
        return Result.success("ok", bmsDailySettlementService.getById(token, id));
    }

    /**
     * 生成日结快照：统计区间内 bms 收款分摊 + 收款流水
     */
    @PostMapping("/generate")
    public Result<BmsDailySettlement> generate(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> body) {
        String rs = body.get("rangeStart");
        String re = body.get("rangeEnd");
        LocalDateTime rangeStart = LocalDateTime.parse(rs.trim(), DT);
        LocalDateTime rangeEnd = LocalDateTime.parse(re.trim(), DT);
        return Result.success("生成成功", bmsDailySettlementService.generate(token, rangeStart, rangeEnd));
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable("id") Long id,
            @RequestParam("token") String token) {
        bmsDailySettlementService.audit(token, id);
        return Result.success("核对成功");
    }
}
