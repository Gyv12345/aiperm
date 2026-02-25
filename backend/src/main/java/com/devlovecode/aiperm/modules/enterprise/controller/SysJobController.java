package com.devlovecode.aiperm.modules.enterprise.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.enterprise.dto.JobDTO;
import com.devlovecode.aiperm.modules.enterprise.service.JobService;
import com.devlovecode.aiperm.modules.enterprise.vo.JobVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/enterprise/job")
@SaCheckLogin
@RequiredArgsConstructor
public class SysJobController {

    private final JobService jobService;

    @Operation(summary = "分页查询定时任务")
    @SaCheckPermission("enterprise:job:list")
    @Log(title = "定时任务管理", operType = OperType.QUERY)
    @GetMapping
    public R<PageResult<JobVO>> list(@Validated({Default.class, Views.Query.class}) JobDTO dto) {
        return R.ok(jobService.queryPage(dto));
    }

    @Operation(summary = "查询定时任务详情")
    @SaCheckPermission("enterprise:job:list")
    @GetMapping("/{id}")
    public R<JobVO> detail(@PathVariable Long id) {
        return R.ok(jobService.findById(id));
    }

    @Operation(summary = "创建定时任务")
    @SaCheckPermission("enterprise:job:create")
    @Log(title = "定时任务管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) JobDTO dto) {
        return R.ok(jobService.create(dto));
    }

    @Operation(summary = "更新定时任务")
    @SaCheckPermission("enterprise:job:update")
    @Log(title = "定时任务管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) JobDTO dto) {
        jobService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除定时任务")
    @SaCheckPermission("enterprise:job:delete")
    @Log(title = "定时任务管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return R.ok();
    }

    @Operation(summary = "暂停定时任务")
    @SaCheckPermission("enterprise:job:update")
    @Log(title = "定时任务管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/pause")
    public R<Void> pause(@PathVariable Long id) {
        jobService.pause(id);
        return R.ok();
    }

    @Operation(summary = "恢复定时任务")
    @SaCheckPermission("enterprise:job:update")
    @Log(title = "定时任务管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/resume")
    public R<Void> resume(@PathVariable Long id) {
        jobService.resume(id);
        return R.ok();
    }
}
