package com.his.domain;

import com.his.annotation.Size;

import java.time.LocalDate;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDate createTime) {
        this.createTime = createTime;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getSkdFlag() {
        return skdFlag;
    }

    public void setSkdFlag(Integer skdFlag) {
        this.skdFlag = skdFlag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRegistrationRankId() {
        return registrationRankId;
    }

    public void setRegistrationRankId(String registrationRankId) {
        this.registrationRankId = registrationRankId;
    }

    private Integer roleId;
    private String registrationRankId;


}