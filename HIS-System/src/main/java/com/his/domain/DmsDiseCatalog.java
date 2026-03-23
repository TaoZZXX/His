package com.his.domain;

import lombok.Data;

@Data
public class DmsDiseCatalog {
    private Long id;

    /** 分类名称 */
    private String name;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}

