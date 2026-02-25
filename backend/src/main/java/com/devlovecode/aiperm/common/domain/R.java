package com.devlovecode.aiperm.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装
 *
 * @param <T> 数据类型
 * @author devlovecode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一返回结果")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "返回消息")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功返回（自定义消息和数据）
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 失败返回（默认消息）
     */
    public static <T> R<T> fail() {
        return new R<>(500, "操作失败", null);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null);
    }

    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败返回（使用错误码枚举）
     */
    public static <T> R<T> fail(com.devlovecode.aiperm.common.enums.ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
