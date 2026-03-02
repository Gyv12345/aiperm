package com.devlovecode.aiperm.modules.notification.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.modules.notification.dto.MessageLogDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import com.devlovecode.aiperm.modules.notification.service.MessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "消息推送记录")
@RestController
@RequestMapping("/enterprise/message-log")
@SaCheckLogin
@RequiredArgsConstructor
public class MessageLogController {

    private final MessageLogService messageLogService;

    @Operation(summary = "分页查询消息推送记录")
    @SaCheckPermission("enterprise:message-log:list")
    @GetMapping
    public R<PageResult<SysMessageLog>> list(@Validated({Default.class, Views.Query.class}) MessageLogDTO dto) {
        return R.ok(messageLogService.queryPage(dto));
    }
}
