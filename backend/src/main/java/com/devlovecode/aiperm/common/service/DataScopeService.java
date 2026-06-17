package com.devlovecode.aiperm.common.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.context.DataScopeContext;
import com.devlovecode.aiperm.common.enums.DataScopeEnum;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 *
 * @author DevLoveCode
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

	private final EntityManager em;

	private final UserRepository userRepo;

	/**
	 * 计算当前登录用户的数据权限上下文（结构化结果，供 Specification 层消费）。
	 *
	 * <p>判定逻辑：
	 * <ul>
	 *   <li>未登录或无角色 → ALL（不过滤）</li>
	 *   <li>超管（isAdmin=1）→ 显式短路返回 ALL，无论角色配置如何</li>
	 *   <li>否则取用户所有启用角色的 MIN(data_scope) 作为最宽范围</li>
	 * </ul>
	 */
	public DataScopeContext getDataScopeContext() {
		if (!StpUtil.isLogin()) {
			return DataScopeContext.all();
		}

		Long userId = StpUtil.getLoginIdAsLong();

		// 超管显式短路：强制全部数据，避免被误配的非 ALL 角色收紧
		if (userRepo.isAdmin(userId)) {
			return DataScopeContext.all();
		}

		Integer dataScope = getMaxDataScope(userId);
		DataScopeEnum scopeEnum = DataScopeEnum.of(dataScope);

		return switch (scopeEnum) {
			case ALL -> DataScopeContext.all();
			case DEPT -> {
				Long deptId = getUserDeptId(userId);
				yield new DataScopeContext(scopeEnum, userId, deptId, Set.of());
			}
			case DEPT_AND_CHILD -> {
				Set<Long> deptIds = getDeptAndChildIds(userId).stream().collect(Collectors.toSet());
				yield new DataScopeContext(scopeEnum, userId, null, deptIds);
			}
			case SELF -> new DataScopeContext(scopeEnum, userId, null, Set.of());
		};
	}

	/**
	 * 构建当前用户的数据权限 SQL（已废弃，保留以兼容旧拦截器逻辑）。
	 * <p>新代码应使用 {@link #getDataScopeContext()} + SpecificationUtils.dataScope。
	 * @param deptAlias 部门表别名
	 * @param userAlias 用户表别名
	 * @return SQL 片段
	 */
	@Deprecated
	public String buildDataScopeSql(String deptAlias, String userAlias) {
		DataScopeContext ctx = getDataScopeContext();
		if (ctx.noFilter()) {
			return "";
		}
		return switch (ctx.scope()) {
			case ALL -> "";
			case DEPT -> String.format(" AND %s.id = %d", deptAlias, ctx.deptId());
			case DEPT_AND_CHILD -> String.format(" AND %s.id IN (%s)", deptAlias,
					ctx.deptIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
			case SELF -> String.format(" AND %s.id = %d", userAlias, ctx.userId());
		};
	}

	private Integer getMaxDataScope(Long userId) {
		Query query = em.createNativeQuery("""
				SELECT MIN(r.data_scope)
				FROM sys_role r
				JOIN sys_user_role ur ON r.id = ur.role_id
				WHERE ur.user_id = ? AND r.deleted = 0 AND r.status = 1
				""");
		query.setParameter(1, userId);
		Object result = query.getSingleResult();
		return result != null ? ((Number) result).intValue() : DataScopeEnum.ALL.getCode();
	}

	private Long getUserDeptId(Long userId) {
		return userRepo.findById(userId).map(SysUser::getDeptId).orElse(0L);
	}

	private List<Long> getDeptAndChildIds(Long userId) {
		Long deptId = getUserDeptId(userId);
		if (deptId == null || deptId == 0) {
			return List.of();
		}

		Query query = em.createNativeQuery("""
				SELECT id FROM sys_dept
				WHERE deleted = 0 AND status = 1
				AND (id = ? OR FIND_IN_SET(?, ancestors))
				""");
		query.setParameter(1, deptId);
		query.setParameter(2, deptId);
		@SuppressWarnings("unchecked")
		List<Number> results = query.getResultList();
		return results.stream().map(Number::longValue).collect(Collectors.toList());
	}

}

