package com.his.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EndAttendanceCode {
    UNFINISHED(0, "未完成"),
    FINISHED(1, "已完成");

    private final int code;
    private final String description;

    EndAttendanceCode(int code, String description) {
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

    public static EndAttendanceCode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EndAttendanceCode e : values()) {
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
