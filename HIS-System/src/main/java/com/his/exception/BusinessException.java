package com.his.exception;

import com.his.enums.ResultCode;

public class BusinessException extends RuntimeException {

    private final ResultCode code;
    private final String msg;

    public BusinessException(ResultCode code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public ResultCode getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

}
