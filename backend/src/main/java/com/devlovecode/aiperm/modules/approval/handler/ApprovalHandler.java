package com.devlovecode.aiperm.modules.approval.handler;

import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;

public interface ApprovalHandler {
    void onApproved(SysApprovalInstance instance);
    void onRejected(SysApprovalInstance instance);
}
