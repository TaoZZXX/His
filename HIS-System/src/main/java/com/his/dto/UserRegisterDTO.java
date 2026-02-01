package com.his.dto;


import com.his.annotation.Size;

public class UserRegisterDTO {

    @Size(filedName = "账号", min = 2, max = 64, message = "长度必须在2-64之间")
    private String username;

    @Size(filedName = "密码", min = 6, max = 64, message = "长度必须在6-64之间")
    private String password;

    @Size(filedName = "确认密码", min = 6, max = 64, message = "长度必须在6-64之间")
    private String confirmPassword;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

