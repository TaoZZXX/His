package com.his.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 挂号记录收费标记（dms_registration.bind_status）
 */
public enum RegistrationBindStatus {

    UNPAID(0, "未缴"),
    PAID(1, "已缴");

    private final int code;
    private final String description;

    RegistrationBindStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RegistrationBindStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RegistrationBindStatus e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public boolean matches(Integer code) {
        return code != null && code == this.code;
    }
}
