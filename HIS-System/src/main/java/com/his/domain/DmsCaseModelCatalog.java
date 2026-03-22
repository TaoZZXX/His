package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DmsCaseModelCatalog {
    private Long id;

    /** 父级目录ID（树形结构） */
    private Long parentId;

    /** 目录层级 */
    private Integer level;

    /** 目录类型 */
    private Integer type;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 关联病历模板ID */
    private Long modelId;

    /** 适用范围（1-个人 2-科室 3-全院） */
    private Integer scope;

    /** 所属人/科室ID */
    private Long ownId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 目录编码 */
    private String code;
}

