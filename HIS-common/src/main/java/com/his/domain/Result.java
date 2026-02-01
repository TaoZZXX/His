package com.his.domain;

import com.his.enums.ResultCode;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Result<T> implements Serializable {
    private Integer code;
    private String status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setStatus(ResultCode.SUCCESS.getStatus());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setStatus(ResultCode.SUCCESS.getStatus());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(ResultCode code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());
        result.setStatus(code.getStatus());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
