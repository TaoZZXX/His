package com.his.vo;

public class SkdRuleItemVo {
    private Long id;
    private Long staffId;
    private String daysOfWeek;
    private Integer status;
    private Long skLimit;
    private Long skRuleId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getStaffId(){return staffId;} public void setStaffId(Long staffId){this.staffId=staffId;}
    public String getDaysOfWeek(){return daysOfWeek;} public void setDaysOfWeek(String daysOfWeek){this.daysOfWeek=daysOfWeek;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public Long getSkLimit(){return skLimit;} public void setSkLimit(Long skLimit){this.skLimit=skLimit;}
    public Long getSkRuleId(){return skRuleId;} public void setSkRuleId(Long skRuleId){this.skRuleId=skRuleId;}
}
