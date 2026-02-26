package com.devlovecode.aiperm.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.BCrypt;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.auth.dto.request.LoginRequest;
import com.devlovecode.aiperm.modules.auth.vo.CaptchaVO;
import com.devlovecode.aiperm.modules.auth.vo.LoginVO;
import com.devlovecode.aiperm.modules.auth.vo.MenuVO;
import com.devlovecode.aiperm.modules.auth.vo.UserInfoVO;
import com.devlovecode.aiperm.modules.system.entity.SysMenu;
import com.devlovecode.aiperm.modules.system.entity.SysUser;
import com.devlovecode.aiperm.modules.system.repository.MenuRepository;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务
 *
 * @author DevLoveCode
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final MenuRepository menuRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE = 5; // 验证码过期时间（分钟）
    private static final Long SUPER_ADMIN_ID = 1L;

    /**
     * 生成验证码
     */
    public CaptchaVO generateCaptcha() {
        // 生成验证码图片
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64Data();

        // 生成验证码Key
        String captchaKey = UUID.fastUUID().toString(true);

        // 存入Redis，5分钟过期
        redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaKey,
                code.toLowerCase(),
                CAPTCHA_EXPIRE,
                TimeUnit.MINUTES
        );

        return CaptchaVO.builder()
                .captchaKey(captchaKey)
                .captchaImage(imageBase64)
                .build();
    }

    /**
     * 登录
     */
    public LoginVO login(LoginRequest request) {
        // 验证码校验（暂时跳过，方便开发调试）
        // validateCaptcha(request.getCaptchaKey(), request.getCaptcha());

        // 查询用户
        SysUser user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 密码校验
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 执行登录
        StpUtil.login(user.getId());

        // 更新登录信息
        userRepo.updateLoginInfo(user.getId(), "127.0.0.1");

        // 返回登录信息
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userInfo(buildUserInfo(user))
                .build();
    }

    /**
     * 登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前用户信息
     */
    public LoginVO.UserInfo getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return buildUserInfo(user);
    }

    /**
     * 获取当前用户完整信息（包含角色和权限）
     */
    public UserInfoVO getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<String> roles;
        List<String> permissions;

        if (SUPER_ADMIN_ID.equals(userId)) {
            // 超级管理员：返回所有启用的权限
            roles = List.of("super_admin");
            permissions = getAllPermissions();
        } else {
            // 普通用户：查询角色和权限
            roles = getUserRoles(userId);
            permissions = getUserPermissions(userId);
        }

        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    /**
     * 获取当前用户可访问的菜单
     */
    public List<MenuVO> getUserMenus() {
        Long userId = StpUtil.getLoginIdAsLong();

        List<SysMenu> menus;
        if (SUPER_ADMIN_ID.equals(userId)) {
            // 超级管理员：返回所有启用的菜单
            menus = menuRepo.findAllEnabled();
        } else {
            // 普通用户：根据角色查询
            List<Long> menuIds = menuRepo.findMenuIdsByUserId(userId);
            menus = menuRepo.findByIds(menuIds);
        }

        // 构建树形结构
        return buildMenuTree(menus, 0L);
    }

    // ========== 私有方法 ==========

    /**
     * 验证码校验
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey == null || captchaKey.isBlank()) {
            throw new BusinessException("验证码Key不能为空");
        }
        if (captcha == null || captcha.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }

        String key = CAPTCHA_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期");
        }

        if (!storedCode.equals(captcha.toLowerCase())) {
            throw new BusinessException("验证码错误");
        }

        // 验证成功后删除验证码
        redisTemplate.delete(key);
    }

    /**
     * 构建用户信息
     */
    private LoginVO.UserInfo buildUserInfo(SysUser user) {
        return LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    /**
     * 获取用户角色
     */
    public List<String> getUserRoles(Long userId) {
        String sql = """
            SELECT r.role_key
            FROM sys_role r
            INNER JOIN sys_user_role ur ON r.id = ur.role_id
            WHERE ur.user_id = :userId AND r.status = 1 AND r.deleted = 0
            """;
        return menuRepo.getJdbcClient().sql(sql)
                .param("userId", userId)
                .query(String.class)
                .list();
    }

    /**
     * 获取用户权限
     */
    public List<String> getUserPermissions(Long userId) {
        // 获取用户所有角色的权限标识
        String sql = """
            SELECT DISTINCT m.perms
            FROM sys_menu m
            INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
            INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = :userId AND m.perms IS NOT NULL AND m.perms != ''
              AND m.status = 1 AND m.deleted = 0
            """;
        return menuRepo.getJdbcClient().sql(sql)
                .param("userId", userId)
                .query(String.class)
                .list();
    }

    /**
     * 获取所有启用的权限（超级管理员使用）
     */
    public List<String> getAllPermissions() {
        String sql = """
            SELECT DISTINCT perms
            FROM sys_menu
            WHERE perms IS NOT NULL AND perms != '' AND status = 1 AND deleted = 0
            """;
        return menuRepo.getJdbcClient().sql(sql)
                .query(String.class)
                .list();
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> allMenus, Long parentId) {
        Map<Long, List<SysMenu>> groupedByParent = allMenus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
        return roots.stream()
                .map(menu -> toMenuVO(menu, groupedByParent))
                .collect(Collectors.toList());
    }

    /**
     * 转换为 MenuVO
     */
    private MenuVO toMenuVO(SysMenu menu, Map<Long, List<SysMenu>> groupedByParent) {
        MenuVO vo = MenuVO.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .component(menu.getComponent())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .sort(menu.getSort())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .build();

        List<SysMenu> children = groupedByParent.getOrDefault(menu.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(child -> toMenuVO(child, groupedByParent))
                    .collect(Collectors.toList()));
        }

        return vo;
    }
}
