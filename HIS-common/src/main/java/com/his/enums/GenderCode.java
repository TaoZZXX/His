package com.his.enums;

public enum GenderCode {
    MALE(1, "男"),
    FEMALE(0, "女");

    private final int code;
    private final String description;

    GenderCode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


}
