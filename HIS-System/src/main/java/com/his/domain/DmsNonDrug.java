package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DmsNonDrug {
    private Long id;

    /** 项目编码 */
    private String code;

    /** 项目名称 */
    private String name;

    /** 规格/项目描述 */
    private String format;

    /** 单价 */
    private BigDecimal price;

    /** 关联收费分类ID */
    private Long expClassId;

    /** 助记码 */
    private String mnemonicCode;

    /** 记录类型（1-检查 2-治疗 3-检验等） */
    private Integer recordType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 关联执行科室ID */
    private Long deptId;
}

