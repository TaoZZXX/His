package com.his.dto;

import java.util.List;
import java.util.Map;

public class SkdRuleCreateDto {
    private Long deptId;
    private String ruleName;
    private String ruleDesc;
    private List<SkdRuleItemDto> items;
    public static class SkdRuleItemDto {
        private Long doctorId;
        private Long limit;
        private Map<String, Map<String, Boolean>> skd;
        public Long getDoctorId(){return doctorId;} public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
        public Long getLimit(){return limit;} public void setLimit(Long limit){this.limit=limit;}
        public Map<String, Map<String, Boolean>> getSkd(){return skd;} public void setSkd(Map<String, Map<String, Boolean>> skd){this.skd=skd;}
    }
    public Long getDeptId(){return deptId;} public void setDeptId(Long deptId){this.deptId=deptId;}
    public String getRuleName(){return ruleName;} public void setRuleName(String ruleName){this.ruleName=ruleName;}
    public String getRuleDesc(){return ruleDesc;} public void setRuleDesc(String ruleDesc){this.ruleDesc=ruleDesc;}
    public List<SkdRuleItemDto> getItems(){return items;} public void setItems(List<SkdRuleItemDto> items){this.items=items;}
}
