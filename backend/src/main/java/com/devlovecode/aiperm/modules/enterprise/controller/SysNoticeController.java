package com.devlovecode.aiperm.modules.enterprise.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.enterprise.dto.NoticeDTO;
import com.devlovecode.aiperm.modules.enterprise.service.NoticeService;
import com.devlovecode.aiperm.modules.enterprise.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公告通知管理")
@RestController
@RequestMapping("/enterprise/notice")
@SaCheckLogin
@RequiredArgsConstructor
public class SysNoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "分页查询公告")
    @SaCheckPermission("enterprise:notice:list")
    @Log(title = "公告通知管理", operType = OperType.QUERY)
    @GetMapping
    public R<PageResult<NoticeVO>> list(@Validated({Default.class, Views.Query.class}) NoticeDTO dto) {
        return R.ok(noticeService.queryPage(dto));
    }

    @Operation(summary = "查询已发布公告")
    @SaCheckPermission("enterprise:notice:list")
    @GetMapping("/published")
    public R<List<NoticeVO>> published(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(noticeService.findPublished(type, limit));
    }

    @Operation(summary = "查询公告详情")
    @SaCheckPermission("enterprise:notice:list")
    @GetMapping("/{id}")
    public R<NoticeVO> detail(@PathVariable Long id) {
        return R.ok(noticeService.findById(id));
    }

    @Operation(summary = "创建公告")
    @SaCheckPermission("enterprise:notice:create")
    @Log(title = "公告通知管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) NoticeDTO dto) {
        return R.ok(noticeService.create(dto));
    }

    @Operation(summary = "更新公告")
    @SaCheckPermission("enterprise:notice:update")
    @Log(title = "公告通知管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) NoticeDTO dto) {
        noticeService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "发布公告")
    @SaCheckPermission("enterprise:notice:publish")
    @Log(title = "公告通知管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return R.ok();
    }

    @Operation(summary = "撤回公告")
    @SaCheckPermission("enterprise:notice:publish")
    @Log(title = "公告通知管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/withdraw")
    public R<Void> withdraw(@PathVariable Long id) {
        noticeService.withdraw(id);
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @SaCheckPermission("enterprise:notice:delete")
    @Log(title = "公告通知管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return R.ok();
    }
}
