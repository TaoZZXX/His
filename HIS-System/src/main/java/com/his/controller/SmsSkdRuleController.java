package com.his.controller;

import com.his.domain.Result;
import com.his.domain.SmsSkdRuleItem;
import com.his.domain.SmsSkdRule;
import com.his.dto.SkdRuleCreateDto;
import com.his.enums.ResultCode;
import com.his.service.ISmsSkdRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registration/skd")
public class SmsSkdRuleController {

    @Autowired
    private ISmsSkdRuleService smsSkdRuleService;

    /**
     * 新增排班规则（草稿）
     */
    @PostMapping("/rules")
    public Result<Map<String, Object>> createRule(@RequestBody SkdRuleCreateDto dto) {
        Long id = smsSkdRuleService.createRule(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增排班规则成功", data);
    }

    /**
     * 按科室查询规则列表
     */
    @GetMapping("/rules")
    public Result<List<SmsSkdRule>> listRules(@RequestParam("deptId") Long deptId) {
        if (deptId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "deptId不能为空");
        }
        return Result.success("查询成功", smsSkdRuleService.listRulesByDept(deptId));
    }

    /**
     * 规则详情（含 items）
     */
    @GetMapping("/rules/{id}")
    public Result<Map<String, Object>> getRuleDetail(@PathVariable("id") Long id) {
        SmsSkdRule rule = smsSkdRuleService.getRule(id);
        List<SmsSkdRuleItem> items = smsSkdRuleService.listRuleItems(id);
        Map<String, Object> data = new HashMap<>();
        data.put("rule", rule);
        data.put("items", items);
        return Result.success("查询成功", data);
    }

    /**
     * 发布规则（仅改状态；如需生成到 sms_skd 可继续补）
     */
    @PostMapping("/rules/{id}/publish")
    public Result<?> publish(@PathVariable("id") Long id) {
        smsSkdRuleService.publishRule(id);
        return Result.success("发布成功");
    }

    /**
     * 根据规则生成排班计划表（写入 sms_skd）
     */
    @PostMapping("/rules/{id}/generate")
    public Result<?> generate(@PathVariable("id") Long id,
                              @RequestBody Map<String, String> body) {
        String start = body.get("startDate");
        String end = body.get("endDate");
        smsSkdRuleService.generateSkdByRule(id, start, end);
        return Result.success("生成排班计划表成功");
    }
}

