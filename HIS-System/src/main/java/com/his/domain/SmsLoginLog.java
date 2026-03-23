package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志表（sms_login_log）
 */
@Data
public class SmsLoginLog {

    private Long id;

    /** 登录用户ID */
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 登录 IP 地址 */
    private String ip;
}
