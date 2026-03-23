package com.his.service;

import com.his.domain.SmsSkdRuleItem;
import com.his.domain.SmsSkdRule;
import com.his.dto.SkdRuleCreateDto;

import java.util.List;

public interface ISmsSkdRuleService {

    Long createRule(SkdRuleCreateDto dto);

    SmsSkdRule getRule(Long id);

    List<SmsSkdRule> listRulesByDept(Long deptId);

    List<SmsSkdRuleItem> listRuleItems(Long ruleId);

    void publishRule(Long ruleId);

    /**
     * 根据规则生成排班计划（写入 sms_skd）
     */
    void generateSkdByRule(Long ruleId, String startDate, String endDate);
}

