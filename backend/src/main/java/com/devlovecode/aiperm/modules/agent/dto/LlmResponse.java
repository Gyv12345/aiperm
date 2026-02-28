package com.devlovecode.aiperm.modules.agent.dto;

import lombok.Data;

import java.util.List;

/**
 * LLM 响应
 */
@Data
public class LlmResponse {
    private String content;
    private List<ToolCall> toolCalls;
    private boolean finished;

    @Data
    public static class ToolCall {
        private String id;
        private String name;
        private String arguments;
    }

    public static LlmResponse text(String content) {
        LlmResponse r = new LlmResponse();
        r.setContent(content);
        r.setFinished(true);
        return r;
    }

    public static LlmResponse toolCall(List<ToolCall> toolCalls) {
        LlmResponse r = new LlmResponse();
        r.setToolCalls(toolCalls);
        r.setFinished(false);
        return r;
    }
}
