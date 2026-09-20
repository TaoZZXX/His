package com.his.vo;

import java.util.List;

public class SkdRuleDetailVo {
    private SkdRuleVo rule;
    private List<SkdRuleItemVo> items;
    public SkdRuleVo getRule(){return rule;} public void setRule(SkdRuleVo rule){this.rule=rule;}
    public List<SkdRuleItemVo> getItems(){return items;} public void setItems(List<SkdRuleItemVo> items){this.items=items;}
}
