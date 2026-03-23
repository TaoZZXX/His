package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 挂号维度可收费明细（部分付款粒度）——表 bms_payable_item
 */
@Data
public class BmsPayableItem {

    private Long id;

    private Long registrationId;

    /**
     * 费用类型：1 非药品(检查检验) 2 成药处方头 3 草药处方头 4 挂号/号别 5 其它
     */
    private Integer itemType;

    /** 来源业务主键 */
    private Long sourceId;

    private String itemName;

    /** 应收金额 */
    private BigDecimal amount;

    /** 已收累计 */
    private BigDecimal paidAmount;

    /** 0 未付清 1 已付清 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
