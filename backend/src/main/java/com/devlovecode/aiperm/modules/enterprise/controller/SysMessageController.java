package com.devlovecode.aiperm.modules.enterprise.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.enterprise.dto.MessageDTO;
import com.devlovecode.aiperm.modules.enterprise.service.MessageService;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageReceiverVO;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息中心管理")
@RestController
@RequestMapping("/enterprise/message")
@SaCheckLogin
@RequiredArgsConstructor
public class SysMessageController {

	private final MessageService messageService;

	@Operation(summary = "分页查询消息")
	@SaCheckPermission("enterprise:message:list")
	@Log(title = "消息中心管理", operType = OperType.QUERY)
	@GetMapping
	public R<PageResult<MessageVO>> list(@Validated({ Default.class, Views.Query.class }) MessageDTO dto) {
		return R.ok(messageService.queryPage(dto));
	}

	@Operation(summary = "查询消息详情")
	@SaCheckPermission("enterprise:message:list")
	@GetMapping("/{id}")
	public R<MessageVO> detail(@PathVariable Long id) {
		return R.ok(messageService.findById(id));
	}

	@Operation(summary = "获取未读消息数量")
	@GetMapping("/unread-count")
	public R<Integer> unreadCount() {
		return R.ok(messageService.getUnreadCount());
	}

	@Operation(summary = "获取消息接收人列表")
	@SaCheckPermission("enterprise:message:send")
	@GetMapping("/receivers")
	public R<List<MessageReceiverVO>> receivers() {
		return R.ok(messageService.listReceivers());
	}

	@Operation(summary = "发送消息")
	@SaCheckPermission("enterprise:message:send")
	@Log(title = "消息中心管理", operType = OperType.CREATE)
	@PostMapping
	public R<Long> send(@RequestBody @Validated({ Default.class, Views.Create.class }) MessageDTO dto) {
		return R.ok(messageService.send(dto));
	}

	@Operation(summary = "标记消息为已读")
	@Log(title = "消息中心管理", operType = OperType.UPDATE)
	@PutMapping("/{id}/read")
	public R<Void> markAsRead(@PathVariable Long id) {
		messageService.markAsRead(id);
		return R.ok();
	}

	@Operation(summary = "批量标记消息为已读")
	@Log(title = "消息中心管理", operType = OperType.UPDATE)
	@PutMapping("/read-all")
	public R<Map<String, Integer>> markAllAsRead() {
		int count = messageService.markAllAsRead();
		return R.ok(Map.of("count", count));
	}

	@Operation(summary = "批量标记指定消息为已读")
	@Log(title = "消息中心管理", operType = OperType.UPDATE)
	@PutMapping("/read-batch")
	public R<Map<String, Integer>> markAsReadByIds(@RequestBody MessageDTO dto) {
		int count = messageService.markAsReadByIds(dto.getIds());
		return R.ok(Map.of("count", count));
	}

	@Operation(summary = "删除消息")
	@Log(title = "消息中心管理", operType = OperType.DELETE)
	@DeleteMapping("/{id}")
	public R<Void> delete(@PathVariable Long id) {
		messageService.delete(id);
		return R.ok();
	}

}
