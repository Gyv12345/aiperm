package com.devlovecode.aiperm.modules.log.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.repository.OperLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperLogService {

    private final OperLogRepository operLogRepo;

    /**
     * 分页查询
     */
    public PageResult<SysOperLog> queryPage(String title, Integer status, int page, int pageSize) {
        return operLogRepo.queryPage(title, status, page, pageSize);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        operLogRepo.deleteById(id);
    }

    /**
     * 清空
     */
    @Transactional
    public void clean() {
        operLogRepo.deleteAll();
    }
}
