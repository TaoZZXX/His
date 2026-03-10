package com.his.annotation.utils;

import com.his.annotation.Size;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;

import java.lang.reflect.Field;

public class FiledLengthValidator {

    public static void validate(Object obj) {

        if (obj == null) {
            throw new IllegalArgumentException("待校验字段不能为空");
        }

        Field[] fields =  obj.getClass().getDeclaredFields();
        for (Field field : fields) {

            if (field.isAnnotationPresent(Size.class)) {
                Size annotation = field.getAnnotation(Size.class);

                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);

                    if (fieldValue == null) {

                        if (annotation.min() > 0) {
                            throw new BusinessException(ResultCode.PARAM_ERROR,
                                    String.format("%s长度过短，长度应在[%d, %d]之间",
                                            annotation.filedName(), annotation.min(), annotation.max())
                            );
                        }
                        continue;
                    }

                    if (!(fieldValue instanceof String)) {
                        throw new IllegalArgumentException(annotation.filedName() + "仅支持字符串类型的字段校验");
                    }

                    String fieldValueStr = (String) fieldValue;
                    int length = fieldValueStr.length();

                    if (length < annotation.min()) {
                        throw new BusinessException(ResultCode.PARAM_ERROR,
                                String.format("%s长度过短，长度应在[%d, %d]之间",
                                        annotation.filedName(), annotation.min(), annotation.max())
                        );
                    }

                    if (length > annotation.max()) {
                        throw new BusinessException(ResultCode.PARAM_ERROR,
                                String.format("%s长度过长，长度应在[%d, %d]之间",
                                        annotation.filedName(), annotation.min(), annotation.max())
                        );
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("获取字段值失败：" + field.getName(), e);
                }

            }
        }

    }

}
