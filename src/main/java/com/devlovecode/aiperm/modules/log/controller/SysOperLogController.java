package com.devlovecode.aiperm.modules.log.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/log/oper")
@SaCheckLogin
@RequiredArgsConstructor
public class SysOperLogController {

    private final OperLogService operLogService;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("log:oper:list")
    @GetMapping
    public R<PageResult<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        return R.ok(operLogService.queryPage(title, status, page, pageSize));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("log:oper:delete")
    @Log(title = "操作日志管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        operLogService.delete(id);
        return R.ok();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("log:oper:delete")
    @Log(title = "操作日志管理", operType = OperType.DELETE)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.clean();
        return R.ok();
    }
}
