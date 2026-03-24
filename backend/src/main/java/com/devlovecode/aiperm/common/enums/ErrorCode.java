package com.devlovecode.aiperm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author DevLoveCode
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误码 1xxx
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(401, "缺少必要参数"),
    PARAM_INVALID(402, "参数格式不正确"),

    // 用户相关错误码 10xxx
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_EXISTS(10002, "用户已存在"),
    USER_PASSWORD_ERROR(10003, "密码错误"),
    USER_ACCOUNT_DISABLED(10004, "账号已被禁用"),
    USER_ACCOUNT_LOCKED(10005, "账号已被锁定"),
    USER_PASSWORD_EXPIRED(10006, "密码已过期"),
    USER_OLD_PASSWORD_ERROR(10007, "原密码错误"),
    USER_PASSWORD_SAME(10008, "新密码不能与原密码相同"),

    // 角色相关错误码 20xxx
    ROLE_NOT_FOUND(20001, "角色不存在"),
    ROLE_EXISTS(20002, "角色已存在"),
    ROLE_HAS_USERS(20003, "角色下存在用户，无法删除"),
    ROLE_HAS_MENUS(20004, "角色已分配菜单权限，无法删除"),

    // 菜单相关错误码 30xxx
    MENU_NOT_FOUND(30001, "菜单不存在"),
    MENU_EXISTS(30002, "菜单已存在"),
    MENU_HAS_CHILDREN(30003, "菜单下存在子菜单，无法删除"),
    MENU_PARENT_NOT_FOUND(30004, "父菜单不存在"),
    MENU_PARENT_ERROR(30005, "不能将自身或子菜单设置为父菜单"),

    // 部门相关错误码 40xxx
    DEPT_NOT_FOUND(40001, "部门不存在"),
    DEPT_EXISTS(40002, "部门已存在"),
    DEPT_HAS_USERS(40003, "部门下存在用户，无法删除"),
    DEPT_HAS_CHILDREN(40004, "部门下存在子部门，无法删除"),
    DEPT_PARENT_NOT_FOUND(40005, "父部门不存在"),
    DEPT_PARENT_ERROR(40006, "不能将自身或子部门设置为父部门"),

    // 权限相关错误码 50xxx
    NO_PERMISSION(50001, "无权限访问"),
    TOKEN_INVALID(50002, "Token无效"),
    TOKEN_EXPIRED(50003, "Token已过期"),
    UNAUTHORIZED(50004, "未登录或登录已过期"),
    FORBIDDEN(50005, "禁止访问"),
    RATE_LIMITED(50006, "请求过于频繁，请稍后重试"),
    IDEMPOTENT_CONFLICT(50007, "请勿重复提交"),

    // 系统错误码 9xxx
    SYSTEM_ERROR(90001, "系统错误"),
    DATABASE_ERROR(90002, "数据库错误"),
    NETWORK_ERROR(90003, "网络错误"),
    FILE_UPLOAD_ERROR(90004, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(90005, "文件下载失败"),
    FILE_TYPE_ERROR(90006, "文件类型不正确"),
    FILE_SIZE_ERROR(90007, "文件大小超出限制");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}
