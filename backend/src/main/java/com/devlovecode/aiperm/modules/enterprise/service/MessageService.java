package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.dto.MessageDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysMessage;
import com.devlovecode.aiperm.modules.enterprise.repository.MessageRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepo;

    /**
     * 分页查询当前用户消息
     */
    public PageResult<MessageVO> queryPage(MessageDTO dto) {
        Long receiverId = getCurrentUserId();
        Specification<SysMessage> spec = SpecificationUtils.and(
                SpecificationUtils.eq("receiverId", receiverId),
                SpecificationUtils.eq("isRead", dto.getIsRead())
        );
        PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
        Page<SysMessage> page = messageRepo.findAll(spec, pageRequest);
        PageResult<SysMessage> result = PageResult.fromJpaPage(page);
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public MessageVO findById(Long id) {
        return messageRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("消息不存在"));
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount() {
        Long userId = getCurrentUserId();
        return (int) messageRepo.countUnread(userId);
    }

    /**
     * 发送消息
     */
    @Transactional
    public Long send(MessageDTO dto) {
        SysMessage entity = new SysMessage();
        entity.setSenderId(getCurrentUserId());
        entity.setReceiverId(dto.getReceiverId());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setIsRead(0);
        entity.setCreateBy(getCurrentUsername());
        entity.setCreateTime(LocalDateTime.now());

        messageRepo.save(entity);

        return entity.getId();
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long id) {
        SysMessage message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        // 验证是否是当前用户的消息
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(message.getReceiverId())) {
            throw new BusinessException("无权操作此消息");
        }

        LocalDateTime now = LocalDateTime.now();
        messageRepo.markAsRead(id, now, now);
    }

    /**
     * 批量标记消息为已读
     */
    @Transactional
    public int markAllAsRead() {
        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        return messageRepo.markAllAsRead(userId, now, now);
    }

    /**
     * 批量标记指定消息为已读
     */
    @Transactional
    public int markAsReadByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return messageRepo.markAsReadByIds(ids, now, now);
    }

    /**
     * 删除消息
     */
    @Transactional
    public void delete(Long id) {
        SysMessage message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        // 验证是否是当前用户的消息
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(message.getReceiverId())) {
            throw new BusinessException("无权操作此消息");
        }

        messageRepo.softDelete(id, LocalDateTime.now());
    }

    // ========== 私有方法 ==========

    private MessageVO toVO(SysMessage entity) {
        MessageVO vo = new MessageVO();
        vo.setId(entity.getId());
        vo.setSenderId(entity.getSenderId());
        vo.setReceiverId(entity.getReceiverId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setIsRead(entity.getIsRead());
        vo.setReadTime(entity.getReadTime());
        vo.setCreateTime(entity.getCreateTime());
        // senderName 可以通过关联查询用户表获取，这里简化处理
        vo.setSenderName(entity.getCreateBy());
        return vo;
    }

    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            throw new BusinessException("请先登录");
        }
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
