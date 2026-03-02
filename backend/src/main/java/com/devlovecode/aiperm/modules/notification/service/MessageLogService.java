package com.devlovecode.aiperm.modules.notification.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.notification.dto.MessageLogDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import com.devlovecode.aiperm.modules.notification.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepo;

    public PageResult<SysMessageLog> queryPage(MessageLogDTO dto) {
        return messageLogRepo.queryPage(dto.getTemplateCode(), dto.getPlatform(), dto.getStatus(), dto.getPage(), dto.getPageSize());
    }
}
