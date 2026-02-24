package com.devlovecode.aiperm.common.entity;

import com.devlovecode.aiperm.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装
 *
 * @param <T> 数据类型
 * @author DevLoveCode
 */
@Data
@Schema(description = "统一返回结果")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private Integer code;

    /**
     * 返回消息
     */
    @Schema(description = "返回消息")
    private String message;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private Long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 成功返回（有数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 失败返回（默认错误码）
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
     * 失败返回（自定义错误码和消息）
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败返回（错误码枚举）
     */
    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回（错误码枚举和数据）
     */
    public static <T> R<T> fail(ErrorCode errorCode, T data) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
}
