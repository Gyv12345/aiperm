package com.devlovecode.aiperm.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devlovecode.aiperm.modules.log.entity.SysOperLog;
import com.devlovecode.aiperm.modules.log.mapper.SysOperLogMapper;
import com.devlovecode.aiperm.modules.log.service.ISysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
        implements ISysOperLogService {
}
