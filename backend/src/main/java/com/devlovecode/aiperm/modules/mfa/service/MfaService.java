package com.devlovecode.aiperm.modules.mfa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.enterprise.repository.ConfigRepository;
import com.devlovecode.aiperm.modules.mfa.entity.SysUserMfa;
import com.devlovecode.aiperm.modules.mfa.repository.UserMfaRepository;
import com.devlovecode.aiperm.modules.mfa.vo.MfaQrcodeVO;
import com.devlovecode.aiperm.modules.mfa.vo.MfaStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 2FA 服务
 *
 * 使用 Hutool 内置的 GoogleAuthenticator 实现 TOTP 验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserMfaRepository userMfaRepo;
    private final ConfigRepository configRepo;
    private final StringRedisTemplate redisTemplate;

    private static final Long SUPER_ADMIN_ID = 1L;
    private static final String MFA_VERIFIED_PREFIX = "mfa:verified:";
    private static final String MFA_TEMP_PREFIX = "mfa:temp:";
    private static final String APP_NAME = "AIPerm";

    /**
     * 获取当前用户的2FA状态
     */
    public MfaStatusVO getStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean bound = userMfaRepo.findByUserId(userId)
                .map(m -> m.getStatus() != null && m.getStatus() == 1)
                .orElse(false);
        boolean required = SUPER_ADMIN_ID.equals(userId);
        boolean verified = isVerified(userId);

        return MfaStatusVO.builder()
                .bound(bound)
                .required(required)
                .verified(verified)
                .build();
    }

    /**
     * 生成绑定二维码（临时密钥存入Redis，绑定确认前不写库）
     */
    public MfaQrcodeVO generateQrCode() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 检查是否已绑定
        boolean alreadyBound = userMfaRepo.findByUserId(userId)
                .map(m -> m.getStatus() != null && m.getStatus() == 1)
                .orElse(false);
        if (alreadyBound) {
            throw new BusinessException("您已绑定2FA，如需重新绑定请先解绑");
        }

        // 生成随机密钥（Base32）
        String secretKey = generateSecretKey();

        // 临时存入 Redis，10 分钟过期（等待用户确认绑定）
        String tempKey = MFA_TEMP_PREFIX + userId;
        redisTemplate.opsForValue().set(tempKey, secretKey, 10, TimeUnit.MINUTES);

        // 生成 TOTP URI（Google Authenticator 格式）
        // 格式：otpauth://totp/AIPerm:username?secret=xxx&issuer=AIPerm
        String username = StpUtil.getLoginIdAsString();
        String totpUri = "otpauth://totp/" + APP_NAME + ":" + username
                + "?secret=" + secretKey + "&issuer=" + APP_NAME;

        return MfaQrcodeVO.builder()
                .totpUri(totpUri)
                .secretKey(secretKey)
                .build();
    }

    /**
     * 确认绑定（验证 TOTP 码正确后持久化密钥）
     */
    @Transactional
    public void confirmBind(String code) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 从 Redis 取出临时密钥
        String tempKey = MFA_TEMP_PREFIX + userId;
        String secretKey = redisTemplate.opsForValue().get(tempKey);
        if (secretKey == null) {
            throw new BusinessException("绑定已超时，请重新获取二维码");
        }

        // 验证 TOTP 码
        if (!verifyTotp(secretKey, code)) {
            throw new BusinessException("验证码错误");
        }

        // 删除临时密钥
        redisTemplate.delete(tempKey);

        // 删除旧绑定（如有）
        userMfaRepo.deleteByUserId(userId);

        // 写入绑定记录
        SysUserMfa mfa = new SysUserMfa();
        mfa.setUserId(userId);
        mfa.setMfaType("TOTP");
        mfa.setSecretKey(secretKey);
        mfa.setBindTime(LocalDateTime.now());
        mfa.setStatus(1);
        mfa.setCreateBy(StpUtil.getLoginIdAsString());
        userMfaRepo.insert(mfa);
    }

    /**
     * 解绑 2FA
     */
    @Transactional
    public void unbind(String code) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 超管不允许解绑
        if (SUPER_ADMIN_ID.equals(userId)) {
            throw new BusinessException("超级管理员不允许解绑2FA");
        }

        SysUserMfa mfa = userMfaRepo.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("您未绑定2FA"));

        // 解绑前需先验证
        if (!verifyTotp(mfa.getSecretKey(), code)) {
            throw new BusinessException("验证码错误");
        }

        userMfaRepo.deleteByUserId(userId);
        redisTemplate.delete(MFA_VERIFIED_PREFIX + userId);
    }

    /**
     * 验证 2FA（敏感操作前调用）
     */
    public void verify(String code) {
        Long userId = StpUtil.getLoginIdAsLong();

        SysUserMfa mfa = userMfaRepo.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("您未绑定2FA，请先绑定"));

        if (!verifyTotp(mfa.getSecretKey(), code)) {
            throw new BusinessException("验证码错误");
        }

        // 写入 Redis 验证状态（有效期可配置，默认30分钟）
        int expireMinutes = getVerifyExpireMinutes();
        redisTemplate.opsForValue().set(
                MFA_VERIFIED_PREFIX + userId,
                "1",
                expireMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * 检查用户是否已在有效期内验证过2FA
     */
    public boolean isVerified(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(MFA_VERIFIED_PREFIX + userId));
    }

    // ===== 私有方法 =====

    /**
     * 生成 Base32 格式的随机密钥
     */
    private String generateSecretKey() {
        // Base32 字符集
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 验证 TOTP 码
     * 使用 HMAC-SHA1 算法，时间窗口为 30 秒
     */
    private boolean verifyTotp(String secretKey, String code) {
        try {
            if (code == null || code.length() != 6) {
                return false;
            }

            // 解码 Base32 密钥
            byte[] key = decodeBase32(secretKey);

            // 获取当前时间窗口
            long currentTimeWindow = System.currentTimeMillis() / 1000 / 30;

            // 允许前后1个窗口的误差（共3个窗口）
            for (int i = -1; i <= 1; i++) {
                long timeWindow = currentTimeWindow + i;
                String expectedCode = generateTotp(key, timeWindow);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("TOTP 验证异常", e);
            return false;
        }
    }

    /**
     * 生成 TOTP 码
     */
    private String generateTotp(byte[] key, long timeWindow) {
        byte[] data = new byte[8];
        long value = timeWindow;
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        // HMAC-SHA1
        javax.crypto.Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] hash = mac.doFinal(data);

        // 动态截断
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    /**
     * Base32 解码
     */
    private byte[] decodeBase32(String base32) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");

        int outputLength = base32.length() * 5 / 8;
        byte[] output = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (char c : base32.toCharArray()) {
            int value = chars.indexOf(c);
            if (value < 0) continue;

            buffer = (buffer << 5) | value;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                output[index++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }

        return output;
    }

    private int getVerifyExpireMinutes() {
        return configRepo.findByConfigKey("mfa.verify_expire_minutes")
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (Exception e) { return 30; }
                })
                .orElse(30);
    }
}
