package com.devlovecode.aiperm.modules.agent.service;

/**
 * 流式响应回调接口
 */
public interface StreamCallback {

    /**
     * 文本片段
     */
    void onText(String delta);

    /**
     * 需要确认
     */
    void onConfirm(String actionId, String toolName, String message);

    /**
     * 工具执行结果
     */
    void onToolResult(String toolName, Object result);

    /**
     * 完成
     */
    void onDone();

    /**
     * 错误
     */
    void onError(Throwable e);
}
