package com.devlovecode.aiperm.modules.log.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.service.ISysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/oper-log")
@SaCheckLogin
@RequiredArgsConstructor
public class SysOperLogController {

    private final ISysOperLogService operLogService;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("log:oper:list")
    @GetMapping("/page")
    public R<PageResult<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .like(title != null, SysOperLog::getTitle, title)
                .eq(status != null, SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getCreateTime);

        Page<SysOperLog> pageResult = operLogService.page(new Page<>(page, pageSize), wrapper);
        return R.ok(PageResult.of(pageResult));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("log:oper:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        operLogService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("log:oper:delete")
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.remove(null);
        return R.ok();
    }
}
