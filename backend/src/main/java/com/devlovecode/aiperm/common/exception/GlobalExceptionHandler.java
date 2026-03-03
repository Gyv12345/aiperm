package com.devlovecode.aiperm.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author devlovecode
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常 [{}]：{}", requestSummary(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * NotLoginException：未登录异常
     * 返回 HTTP 401 状态码，触发前端重新登录
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLoginException(NotLoginException e) {
        log.error("未登录异常 [{}]：{}", requestSummary(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(R.fail(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期，请重新登录"));
    }

    /**
     * NotPermissionException：无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e) {
        log.error("无权限异常 [{}]：{}", requestSummary(), e.getMessage());
        return R.fail(ErrorCode.FORBIDDEN.getCode(), "无权限访问");
    }

    /**
     * NotRoleException：无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Void> handleNotRoleException(NotRoleException e) {
        log.error("无角色异常 [{}]：{}", requestSummary(), e.getMessage());
        return R.fail(ErrorCode.FORBIDDEN.getCode(), "无权限访问");
    }

    /**
     * MethodArgumentNotValidException：参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常 [{}]：{}", requestSummary(), errorMsg);
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * BindException：参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常 [{}]：{}", requestSummary(), errorMsg);
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * IllegalArgumentException：非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常 [{}]：{}", requestSummary(), e.getMessage());
        return R.fail(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 其他未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常 [{}]", requestSummary(), e);
        return R.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常，请联系管理员");
    }

    private String requestSummary() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "N/A";
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getMethod() + " " + request.getRequestURI();
    }
}
