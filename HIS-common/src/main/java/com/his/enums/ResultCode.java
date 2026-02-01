package com.his.enums;

public enum ResultCode {
    SUCCESS(20000, "success"),
    LOGIN_FAILED(401, "error"),
    PARAM_ERROR(400, "error"),
    SERVER_ERROR(500, "error");

    private final Integer code;
    private final String status;

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    private ResultCode(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

}
