package com.his.annotation;


import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Size {
    String filedName();
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "字段长度不符合要求";
}
