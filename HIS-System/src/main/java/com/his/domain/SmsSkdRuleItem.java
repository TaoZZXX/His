package com.his.domain;

import lombok.Data;

@Data
public class SmsSkdRuleItem {
    private Long id;
    private Long staffId;
    private String daysOfWeek;
    private Integer status;
    private Long skLimit;
    private Long skRuleId;
}
