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
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("emailCaptchaService")
@RequiredArgsConstructor
public class EmailCaptchaService implements CaptchaService {

    private final CaptchaConfigRepository captchaConfigRepo;
    private final CaptchaLogRepository captchaLogRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:email:";
    private static final String RATE_LIMIT_PREFIX = "captcha:rate:email:";
    private static final int RATE_LIMIT_SECONDS = 60;

    @Override
    public void send(String target, CaptchaScene scene, String ip) {
        SysCaptchaConfig config = captchaConfigRepo.findByType("EMAIL")
                .orElseThrow(() -> new BusinessException("邮件服务未配置"));
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            throw new BusinessException("邮件服务未启用");
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
        logRecord.setType("EMAIL");
        logRecord.setTarget(target);
        logRecord.setCode(code);
        logRecord.setScene(scene.getCode());
        logRecord.setIp(ip);

        try {
            doSendEmail(config, target, code, scene);
            logRecord.setStatus(1);
        } catch (Exception e) {
            log.error("邮件发送失败，target={}", target, e);
            logRecord.setStatus(0);
            logRecord.setFailReason(e.getMessage());
            captchaLogRepo.insert(logRecord);
            throw new BusinessException("邮件发送失败：" + e.getMessage());
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

    private void doSendEmail(SysCaptchaConfig config, String to, String code, CaptchaScene scene) throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getEmailHost());
        sender.setPort(config.getEmailPort() != null ? config.getEmailPort() : 465);
        sender.setUsername(config.getEmailUsername());
        sender.setPassword(config.getEmailPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.timeout", "5000");
        sender.setJavaMailProperties(props);

        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, false, "UTF-8");
        String fromName = config.getEmailFromName() != null ? config.getEmailFromName() : "AIPerm";
        helper.setFrom(config.getEmailFrom(), fromName);
        helper.setTo(to);
        helper.setSubject("【" + fromName + "】验证码");
        helper.setText(buildEmailContent(code, scene, config.getExpireMinutes()), true);

        sender.send(message);
    }

    private String buildEmailContent(String code, CaptchaScene scene, Integer expireMinutes) {
        int expire = expireMinutes != null ? expireMinutes : 5;
        return "<div style='font-family:sans-serif;padding:20px'>"
                + "<h3>您的验证码</h3>"
                + "<p>验证场景：" + scene.getDesc() + "</p>"
                + "<p style='font-size:32px;font-weight:bold;color:#2563eb;letter-spacing:8px'>" + code + "</p>"
                + "<p>验证码有效期 " + expire + " 分钟，请勿泄露给他人。</p>"
                + "</div>";
    }
}
