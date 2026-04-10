package com.devlovecode.aiperm.modules.system.rbac.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("字典数据缓存注解测试")
class DictDataServiceCacheAnnotationTest {

    @Test
    @DisplayName("根据字典类型查询应使用位置参数作为缓存 key")
    void listByDictTypeShouldUsePositionalCacheKey() throws NoSuchMethodException {
        Method method = DictDataService.class.getMethod("listByDictType", String.class);
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertEquals("#p0", cacheable.key());
    }
}
