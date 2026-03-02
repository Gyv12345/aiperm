package com.devlovecode.aiperm.modules.dashboard.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.modules.dashboard.vo.DashboardStatsVO;
import com.devlovecode.aiperm.modules.system.repository.MenuRepository;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import com.devlovecode.aiperm.modules.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final MenuRepository menuRepo;

    /**
     * 获取首页统计数据
     */
    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        // 用户总数
        vo.setUserCount(userRepo.count());

        // 角色数量
        vo.setRoleCount(roleRepo.count());

        // 菜单/权限数量
        vo.setMenuCount(menuRepo.count());

        // 在线用户数（从 Sa-Token 获取）
        vo.setOnlineCount(getOnlineCount());

        return vo;
    }

    /**
     * 获取在线用户数
     * 当前 Sa-Token 版本不支持按 loginId 直接检索，这里按 token 数量统计在线会话数
     */
    private Long getOnlineCount() {
        try {
            List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, false);
            return tokenList == null ? 0L : (long) tokenList.size();
        } catch (Exception e) {
            log.warn("获取在线用户数失败: {}", e.getMessage());
            return 0L;
        }
    }
}
