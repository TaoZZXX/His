package com.his.exception;

import com.his.domain.Result;
import com.his.enums.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMsg());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < bindingResult.getFieldErrors().size(); i++) {
            if (i != bindingResult.getFieldErrors().size() - 1) {
                stringBuilder.append(bindingResult.getFieldErrors().get(i).getDefaultMessage()).append(",");
            } else {
                stringBuilder.append(bindingResult.getFieldErrors().get(i).getDefaultMessage());
            }
        }

        return Result.error(ResultCode.PARAM_ERROR, stringBuilder.toString());
    }

    // 捕捉系统通用异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(ResultCode.SERVER_ERROR, "系统内部错误，请联系管理员");
    }

}
