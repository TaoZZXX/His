package com.his.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 挂号号别表（sms_registration_rank）
 */
@Data
public class SmsRegistrationRank {

    private Long id;

    /** 号别编码 */
    private String code;

    /** 号别名称 */
    private String name;

    /** 排序号 */
    private Long seqNo;

    /** 挂号费用 */
    private BigDecimal price;

    /** 状态（0 禁用 / 1 启用） */
    private Integer status;
}
