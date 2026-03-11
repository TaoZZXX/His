package com.his.domain;

import com.his.annotation.Size;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
public class SmsStaff {

    private Long id;

    @Size(filedName = "账号", min = 2, max = 64, message = "长度必须在2-64之间")
    private String username;

    @Size(filedName = "密码", min = 6, max = 64, message = "长度必须在6-64之间")
    private String password;

    private Integer status;

    private LocalDate createTime;

    private Integer gender;

    private Integer skdFlag;

    @Size(filedName = "", max = 64, message = "长度必须在0-64之间")
    private String title;

    @Size(filedName = "", max = 64, message = "长度必须在0-64之间")
    private String name;

    private Integer deptId;

    private Integer roleId;

    private String registrationRankId;


}