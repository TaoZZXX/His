package com.his.domain;

import lombok.Data;

@Data
public class SmsDept {
    private Long id;

    private String code;

    private Integer catId;

    private String name;

    private Integer type;

    private Integer status;
}
