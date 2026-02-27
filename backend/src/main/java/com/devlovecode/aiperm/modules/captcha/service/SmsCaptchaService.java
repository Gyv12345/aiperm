package com.devlovecode.aiperm.modules.captcha.service;

import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaConfig;
import com.devlovecode.aiperm.modules.captcha.entity.SysCaptchaLog;
import com.devlovecode.aiperm.modules.captcha.enums.CaptchaScene;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaConfigRepository;
import com.devlovecode.aiperm.modules.captcha.repository.CaptchaLogRepository;
import com.devlovecode.aiperm.modules.captcha.util.CaptchaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service("smsCaptchaService")
@RequiredArgsConstructor
public class SmsCaptchaService implements CaptchaService {

    private final CaptchaConfigRepository captchaConfigRepo;
    private final CaptchaLogRepository captchaLogRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:sms:";
    private static final String RATE_LIMIT_PREFIX = "captcha:rate:sms:";
    private static final int RATE_LIMIT_SECONDS = 60;

    @Override
    public void send(String target, CaptchaScene scene, String ip) {
        SysCaptchaConfig config = captchaConfigRepo.findByType("SMS")
                .orElseThrow(() -> new BusinessException("短信服务未配置"));
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("短信服务未启用");
        }

        String rateLimitKey = RATE_LIMIT_PREFIX + target;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new BusinessException("发送太频繁，请60秒后重试");
        }

        int dailyLimit = config.getDailyLimit() != null ? config.getDailyLimit() : 10;
        if (captchaLogRepo.countTodayByTarget(target) >= dailyLimit) {
            throw new BusinessException("今日发送次数已达上限");
        }

        int length = config.getCodeLength() != null ? config.getCodeLength() : 6;
        String code = CaptchaUtil.generateCode(length);

        SysCaptchaLog logRecord = new SysCaptchaLog();
        logRecord.setType("SMS");
        logRecord.setTarget(target);
        logRecord.setCode(code);
        logRecord.setScene(scene.getCode());
        logRecord.setIp(ip);

        try {
            doSendSms(config, target, code);
            logRecord.setStatus(1);
        } catch (Exception e) {
            log.error("短信发送失败，target={}", target, e);
            logRecord.setStatus(0);
            logRecord.setFailReason(e.getMessage());
            captchaLogRepo.insert(logRecord);
            throw new BusinessException("短信发送失败：" + e.getMessage());
        }
        captchaLogRepo.insert(logRecord);

        int expireMinutes = config.getExpireMinutes() != null ? config.getExpireMinutes() : 5;
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        redisTemplate.opsForValue().set(cacheKey, code, expireMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean verify(String target, String code, CaptchaScene scene) {
        String cacheKey = CAPTCHA_KEY_PREFIX + target + ":" + scene.getCode();
        String storedCode = redisTemplate.opsForValue().get(cacheKey);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.equals(code)) {
            redisTemplate.delete(cacheKey);
            return true;
        }
        return false;
    }

    private void doSendSms(SysCaptchaConfig config, String phone, String code) {
        // TODO: 集成 sms4j 发送短信，需在 application.yaml 中配置短信服务商
        // 当前版本先抛出异常，待配置完成后实现
        log.info("模拟发送短信验证码：phone={}, code={}", phone, code);
        // 实际发送逻辑：
        // SmsBlend smsBlend = SmsFactory.getSmsBlend("aliyun");
        // Map<String, Object> params = new LinkedHashMap<>();
        // params.put("code", code);
        // smsBlend.sendMessage(phone, config.getSmsTemplateCode(), params);
    }
}
