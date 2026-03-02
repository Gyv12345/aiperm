package com.devlovecode.aiperm.modules.notification.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.notification.dto.MessageTemplateDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageTemplate;
import com.devlovecode.aiperm.modules.notification.service.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息模板管理")
@RestController
@RequestMapping("/enterprise/message-template")
@SaCheckLogin
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    @Operation(summary = "分页查询消息模板")
    @SaCheckPermission("enterprise:message-template:list")
    @GetMapping
    public R<PageResult<SysMessageTemplate>> list(@Validated({Default.class, Views.Query.class}) MessageTemplateDTO dto) {
        return R.ok(templateService.queryPage(dto));
    }

    @Operation(summary = "查询消息模板详情")
    @SaCheckPermission("enterprise:message-template:list")
    @GetMapping("/{id}")
    public R<SysMessageTemplate> detail(@PathVariable Long id) {
        return R.ok(templateService.findById(id));
    }

    @Operation(summary = "创建消息模板")
    @SaCheckPermission("enterprise:message-template:create")
    @Log(title = "消息模板管理", operType = OperType.CREATE)
    @PostMapping
    public R<Void> create(@RequestBody @Validated({Default.class, Views.Create.class}) MessageTemplateDTO dto) {
        templateService.create(dto);
        return R.ok();
    }

    @Operation(summary = "更新消息模板")
    @SaCheckPermission("enterprise:message-template:update")
    @Log(title = "消息模板管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) MessageTemplateDTO dto) {
        templateService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除消息模板")
    @SaCheckPermission("enterprise:message-template:delete")
    @Log(title = "消息模板管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return R.ok();
    }
}
