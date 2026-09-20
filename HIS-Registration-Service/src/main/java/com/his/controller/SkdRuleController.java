package com.his.controller;

import com.his.domain.Result;
import com.his.dto.DateRangeDto;
import com.his.dto.SkdRuleCreateDto;
import com.his.service.ISkdRuleService;
import com.his.vo.IdResponseVo;
import com.his.vo.SkdRuleDetailVo;
import com.his.vo.SkdRuleVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registration/skd")
public class SkdRuleController {
    private final ISkdRuleService skdRuleService;
    public SkdRuleController(ISkdRuleService skdRuleService) { this.skdRuleService = skdRuleService; }

    @PostMapping("/rules")
    public Result<IdResponseVo> createRule(@RequestBody SkdRuleCreateDto dto) {
        return skdRuleService.createRule(dto);
    }

    @GetMapping("/rules")
    public Result<List<SkdRuleVo>> listRules(@RequestParam("deptId") Long deptId) {
        return skdRuleService.listRules(deptId);
    }

    @GetMapping("/rules/{id}")
    public Result<SkdRuleDetailVo> getRuleDetail(@PathVariable("id") Long id) {
        return skdRuleService.getRuleDetail(id);
    }

    @PostMapping("/rules/{id}/publish")
    public Result<Object> publish(@PathVariable("id") Long id) { return skdRuleService.publish(id); }

    @PostMapping("/rules/{id}/generate")
    public Result<Object> generate(@PathVariable("id") Long id, @RequestBody DateRangeDto body) {
        return skdRuleService.generate(id, body);
    }
}
