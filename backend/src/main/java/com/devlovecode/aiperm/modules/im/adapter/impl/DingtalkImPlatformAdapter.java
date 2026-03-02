package com.devlovecode.aiperm.modules.im.adapter.impl;

import org.springframework.stereotype.Component;

@Component
public class DingtalkImPlatformAdapter extends AbstractImPlatformAdapter {
    @Override
    public String getPlatform() {
        return "DINGTALK";
    }
}
