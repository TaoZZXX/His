package com.his.vo;

import java.time.LocalDateTime;

public class SkdRuleVo {
    private Long id;
    private Integer status;
    private Long operatorId;
    private LocalDateTime operateTime;
    private String ruleName;
    private String description;
    private Long deptId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public Long getOperatorId(){return operatorId;} public void setOperatorId(Long operatorId){this.operatorId=operatorId;}
    public LocalDateTime getOperateTime(){return operateTime;} public void setOperateTime(LocalDateTime operateTime){this.operateTime=operateTime;}
    public String getRuleName(){return ruleName;} public void setRuleName(String ruleName){this.ruleName=ruleName;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public Long getDeptId(){return deptId;} public void setDeptId(Long deptId){this.deptId=deptId;}
}
