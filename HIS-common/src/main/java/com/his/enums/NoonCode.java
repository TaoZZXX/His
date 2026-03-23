package com.his.enums;

/**
 * 午别编码：用于 sms_skd.noon
 * 上午=0，下午=1
 */
public enum NoonCode {
    MORNING(0, "上午"),
    AFTERNOON(1, "下午");

    private final Integer code;
    private final String description;

    NoonCode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

