package com.his.vo;

import lombok.Data;

@Data
public class StaffPageVo {
    private Long id;
    private String username;
    private String name;
    private Long roleId;
    private Long deptId;
    private String deptName;
    private String createTime;

}
