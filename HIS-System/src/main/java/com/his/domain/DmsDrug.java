package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DmsDrug {
    private Long id;

    /** 药品编码 */
    private String code;
    /** 药品名称 */
    private String name;
    /** 规格 */
    private String format;
    /** 单价 */
    private BigDecimal price;
    /** 单位 */
    private String unit;
    /** 生产厂家 */
    private String manufacturer;

    /** 关联剂型ID */
    private Long dosageId;
    /** 关联药品类型ID */
    private Long typeId;

    /** 助记码 */
    private String mnemonicCode;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    /** 库存数量 */
    private Long stock;

    /** 通用名 */
    private String genericName;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}

