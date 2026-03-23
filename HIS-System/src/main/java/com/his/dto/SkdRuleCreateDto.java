package com.his.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SkdRuleCreateDto {
    private Long deptId;
    private String ruleName;
    private String ruleDesc;

    /**
     * items: [{ doctorId, limit, skd: { mon:{am,pm}, ... } }]
     */
    private List<SkdRuleItemDto> items;

    @Data
    public static class SkdRuleItemDto {
        private Long doctorId;
        private Long limit;
        private Map<String, Map<String, Boolean>> skd;
    }
}

