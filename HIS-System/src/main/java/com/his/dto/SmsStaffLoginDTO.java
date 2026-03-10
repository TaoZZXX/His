package com.his.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class SmsStaffLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "用户密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
