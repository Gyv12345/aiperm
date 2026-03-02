package com.devlovecode.aiperm.modules.approval.handler;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("defaultApprovalHandler")
public class DefaultApprovalHandler implements ApprovalHandler {
    @Override
    public void onApproved(SysApprovalInstance instance) {
        log.info("Approval approved with default handler, businessType={}, businessId={}",
                instance.getBusinessType(), instance.getBusinessId());
    }

    @Override
    public void onRejected(SysApprovalInstance instance) {
        log.info("Approval rejected with default handler, businessType={}, businessId={}",
                instance.getBusinessType(), instance.getBusinessId());
    }
}
