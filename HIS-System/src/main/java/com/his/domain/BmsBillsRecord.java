package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BmsBillsRecord {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账单类型
     */
    private Integer type;

    /**
     * 账单编号
     */
    private String billNo;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 状态（0-未处理 1-已处理等）
     */
    private Integer status;

    /**
     * 发票数量
     */
    private Integer invoiceNum;

    /**
     * 关联挂号ID
     */
    private Long registrationId;

    /**
     * 记录列表（JSON/逗号分隔等）
     */
    private String recordList;
}

