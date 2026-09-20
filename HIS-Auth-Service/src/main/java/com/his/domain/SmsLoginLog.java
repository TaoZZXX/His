package com.his.domain;

import java.time.LocalDateTime;

public class SmsLoginLog {
    private Long id;
    private Long userId;
    private LocalDateTime createTime;
    private String ip;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
