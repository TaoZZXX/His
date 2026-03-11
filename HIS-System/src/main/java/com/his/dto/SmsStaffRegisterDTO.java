package com.his.dto;


import com.his.annotation.Size;
import lombok.Data;

@Data
public class SmsStaffRegisterDTO {

    @Size(filedName = "账号", min = 2, max = 64, message = "长度必须在2-64之间")
    private String username;

    @Size(filedName = "密码", min = 6, max = 64, message = "长度必须在6-64之间")
    private String password;

    @Size(filedName = "确认密码", min = 6, max = 64, message = "长度必须在6-64之间")
    private String confirmPassword;
}

