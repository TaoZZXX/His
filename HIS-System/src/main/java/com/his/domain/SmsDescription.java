package com.his.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述信息表（sms_description）
 */
@Data
public class SmsDescription {

    private Long id;

    /** 描述内容 */
    private String description;

    /** 类型 */
    private Integer type;

    /** 状态 */
    private Integer status;

    /** 关联链接 */
    private String url;

    /** 关联业务ID（库列 the_id） */
    private Long theId;

    /** 评分/星级 */
    private BigDecimal star;
}
