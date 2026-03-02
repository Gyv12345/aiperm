package com.devlovecode.aiperm.modules.im.adapter.impl;

import org.springframework.stereotype.Component;

@Component
public class WeworkImPlatformAdapter extends AbstractImPlatformAdapter {
    @Override
    public String getPlatform() {
        return "WEWORK";
    }
}
