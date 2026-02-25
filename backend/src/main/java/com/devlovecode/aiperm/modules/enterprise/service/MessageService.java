package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.dto.MessageDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysMessage;
import com.devlovecode.aiperm.modules.enterprise.repository.MessageRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        PageResult<SysMessage> result = messageRepo.queryPage(
                receiverId, dto.getIsRead(),
                dto.getPage(), dto.getPageSize()
        );
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
        return messageRepo.countUnread(userId);
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

        messageRepo.insert(entity);

        // 返回消息ID（简化处理，通过查询获取）
        return messageRepo.findByReceiverId(dto.getReceiverId()).stream()
                .filter(m -> m.getTitle().equals(dto.getTitle()))
                .findFirst()
                .map(SysMessage::getId)
                .orElse(null);
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

        messageRepo.markAsRead(id);
    }

    /**
     * 批量标记消息为已读
     */
    @Transactional
    public int markAllAsRead() {
        Long userId = getCurrentUserId();
        return messageRepo.markAllAsRead(userId);
    }

    /**
     * 批量标记指定消息为已读
     */
    @Transactional
    public int markAsReadByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return messageRepo.markAsReadByIds(ids);
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

        messageRepo.deleteById(id);
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
