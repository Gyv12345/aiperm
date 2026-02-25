package com.devlovecode.aiperm.common.annotation;

import com.devlovecode.aiperm.common.enums.OperType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 操作模块名称，如"用户管理" */
    String title() default "";

    /** 操作类型 */
    OperType operType() default OperType.OTHER;

    /** 是否保存请求参数，默认 true */
    boolean saveRequestParam() default true;

    /** 是否保存响应结果，默认 false（避免响应体过大） */
    boolean saveResponseResult() default false;
}
