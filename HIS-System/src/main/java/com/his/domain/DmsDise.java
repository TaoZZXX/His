package com.his.domain;

import lombok.Data;

@Data
public class DmsDise {
    private Long id;

    /** 关联疾病分类ID */
    private Long catId;

    /** 疾病编码 */
    private String code;

    /** 疾病名称 */
    private String name;

    /** ICD编码 */
    private String icd;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}

