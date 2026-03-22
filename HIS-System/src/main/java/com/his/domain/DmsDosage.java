package com.his.domain;

import lombok.Data;

@Data
public class DmsDosage {
    private Long id;

    /** 剂型编码 */
    private String code;

    /** 剂型名称（如片剂、胶囊剂、颗粒剂等） */
    private String name;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}

