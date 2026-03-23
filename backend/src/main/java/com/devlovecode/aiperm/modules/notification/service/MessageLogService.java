package com.devlovecode.aiperm.modules.notification.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.notification.dto.MessageLogDTO;
import com.devlovecode.aiperm.modules.notification.entity.SysMessageLog;
import com.devlovecode.aiperm.modules.notification.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepo;

    public PageResult<SysMessageLog> queryPage(MessageLogDTO dto) {
        Specification<SysMessageLog> spec = SpecificationUtils.and(
                SpecificationUtils.like("templateCode", dto.getTemplateCode()),
                SpecificationUtils.eq("platform", dto.getPlatform()),
                SpecificationUtils.eq("status", dto.getStatus())
        );
        PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize());
        Page<SysMessageLog> page = messageLogRepo.findAll(spec, pageRequest);
        return PageResult.fromJpaPage(page);
    }
}
