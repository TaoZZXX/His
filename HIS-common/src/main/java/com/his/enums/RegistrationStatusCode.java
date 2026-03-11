package com.his.enums;

public enum RegistrationStatusCode {
    UNFINISHED(0, "未完成"),
    FINISHED(1, "已完成"),
    CANCELED(2, "已取消");

    private final int code;
    private final String description;

    RegistrationStatusCode(int code, String description) {
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
