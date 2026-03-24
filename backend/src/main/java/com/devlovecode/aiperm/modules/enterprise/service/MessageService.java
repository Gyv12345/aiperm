package com.devlovecode.aiperm.modules.enterprise.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.enterprise.dto.MessageDTO;
import com.devlovecode.aiperm.modules.enterprise.entity.SysMessage;
import com.devlovecode.aiperm.modules.enterprise.repository.MessageRepository;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageReceiverVO;
import com.devlovecode.aiperm.modules.enterprise.vo.MessageVO;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    /**
     * 分页查询当前用户消息
     */
    public PageResult<MessageVO> queryPage(MessageDTO dto) {
        Long currentUserId = getCurrentUserId();
        boolean outbox = Integer.valueOf(2).equals(dto.getBoxType());
        Specification<SysMessage> spec = SpecificationUtils.and(
                SpecificationUtils.eq(outbox ? "senderId" : "receiverId", currentUserId),
                SpecificationUtils.eq("isRead", dto.getIsRead())
        );
        PageRequest pageRequest = PageRequest.of(
                dto.getPage() - 1,
                dto.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        Page<SysMessage> page = messageRepo.findAll(spec, pageRequest);
        PageResult<SysMessage> result = PageResult.fromJpaPage(page);
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public MessageVO findById(Long id) {
        SysMessage message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        Long currentUserId = getCurrentUserId();
        boolean canAccess = currentUserId.equals(message.getReceiverId()) || currentUserId.equals(message.getSenderId());
        if (!canAccess) {
            throw new BusinessException("无权查看此消息");
        }

        return toVO(message);
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount() {
        Long userId = getCurrentUserId();
        return (int) messageRepo.countUnread(userId);
    }

    /**
     * 查询可选接收人列表
     */
    public List<MessageReceiverVO> listReceivers() {
        Long currentUserId = getCurrentUserId();
        return userRepo.findAll()
                .stream()
                .filter(user -> Integer.valueOf(1).equals(user.getStatus()))
                .filter(user -> !user.getId().equals(currentUserId))
                .sorted(Comparator
                        .comparing((SysUser user) -> safeSortName(user.getRealName()))
                        .thenComparing(user -> safeSortName(user.getNickname()))
                        .thenComparing(user -> safeSortName(user.getUsername())))
                .map(this::toReceiverVO)
                .toList();
    }

    /**
     * 发送消息
     */
    @Transactional
    public Long send(MessageDTO dto) {
        Long senderId = getCurrentUserId();
        if (senderId.equals(dto.getReceiverId())) {
            throw new BusinessException("不能给自己发送消息");
        }

        SysUser receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new BusinessException("接收人不存在"));
        if (!Integer.valueOf(1).equals(receiver.getStatus())) {
            throw new BusinessException("接收人状态异常，无法发送消息");
        }

        SysMessage entity = new SysMessage();
        entity.setSenderId(senderId);
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
        vo.setSenderName(resolveUserDisplayName(entity.getSenderId(), entity.getCreateBy()));
        vo.setReceiverName(resolveUserDisplayName(entity.getReceiverId(), null));
        return vo;
    }

    private MessageReceiverVO toReceiverVO(SysUser user) {
        MessageReceiverVO vo = new MessageReceiverVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setDisplayName(pickDisplayName(user));
        return vo;
    }

    private String pickDisplayName(SysUser user) {
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return String.valueOf(user.getId());
    }

    private String resolveUserDisplayName(Long userId, String fallback) {
        if (userId == null) {
            return fallback != null ? fallback : "-";
        }
        return userRepo.findById(userId)
                .map(this::pickDisplayName)
                .orElseGet(() -> fallback != null ? fallback : String.valueOf(userId));
    }

    private String safeSortName(String value) {
        return value == null ? "" : value;
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
