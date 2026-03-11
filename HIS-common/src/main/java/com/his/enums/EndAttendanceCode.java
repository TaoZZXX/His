package com.his.enums;


public enum EndAttendanceCode {
    UNFINISHED(0, "未完成"),
    FINISHED(1, "已完成");

    private Integer code;
    private String description;

    EndAttendanceCode(Integer code, String description) {
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
