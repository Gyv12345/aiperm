package com.devlovecode.aiperm.modules.im.adapter;

import com.devlovecode.aiperm.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImPlatformAdapterFactory {

    private final List<ImPlatformAdapter> adapters;
    private Map<String, ImPlatformAdapter> adapterMap;

    @PostConstruct
    public void init() {
        adapterMap = adapters.stream()
                .collect(Collectors.toMap(a -> a.getPlatform().toUpperCase(Locale.ROOT), Function.identity()));
    }

    public ImPlatformAdapter getAdapter(String platform) {
        String key = platform == null ? "" : platform.toUpperCase(Locale.ROOT);
        ImPlatformAdapter adapter = adapterMap.get(key);
        if (adapter == null) {
            throw new BusinessException("不支持的平台: " + platform);
        }
        return adapter;
    }
}
