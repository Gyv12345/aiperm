package com.devlovecode.aiperm.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token持久层接口（使用Redis实现）
 *
 * @author DevLoveCode
 */
@Component
public class SaTokenInterface implements SaTokenDao {

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * 获取Value，如无返null
     */
    @Override
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout, Object... args) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        if (timeout <= 0) {
            valueOperations.set(key, value);
        } else {
            valueOperations.set(key, value, Duration.ofSeconds(timeout));
        }
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object value, long timeout, Object... args) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        if (timeout <= 0) {
            valueOperations.set(key, value);
        } else {
            valueOperations.set(key, value, Duration.ofSeconds(timeout));
        }
    }

    /**
     * 更新Value (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        if (expire == NOT_VALUE_EXPIRE) {
            set(key, value, 0);
        } else {
            set(key, value, expire);
        }
    }

    /**
     * 更新Value (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object value) {
        long expire = getTimeout(key);
        if (expire == NOT_VALUE_EXPIRE) {
            setObject(key, value, 0);
        } else {
            setObject(key, value, expire);
        }
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 获取剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire == null ? NOT_VALUE_EXPIRE : expire;
    }

    /**
     * 修改剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        redisTemplate.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key, Object defaultValue) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 搜索key
     */
    @Override
    public Set<String> searchKeys(String prefix, String keyword, int start, int size) {
        Set<String> keys = redisTemplate.keys(prefix + "*" + keyword + "*");
        return SaFoxUtil.searchSet(keys, start, size);
    }

    /**
     * 获取List
     */
    @Override
    public List<String> getList(String key) {
        return (List<String>) redisTemplate.opsForValue().get(key);
    }
}
