package com.devlovecode.aiperm.common.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.enums.DataScopeEnum;
import com.devlovecode.aiperm.modules.system.rbac.entity.SysUser;
import com.devlovecode.aiperm.modules.system.rbac.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
	 * 构建当前用户的数据权限 SQL
	 * @param deptAlias 部门表别名
	 * @param userAlias 用户表别名
	 * @return SQL 片段
	 */
	public String buildDataScopeSql(String deptAlias, String userAlias) {
		if (!StpUtil.isLogin()) {
			return "";
		}

		Long userId = StpUtil.getLoginIdAsLong();
		Integer dataScope = getMaxDataScope(userId);
		DataScopeEnum scopeEnum = DataScopeEnum.of(dataScope);

		return switch (scopeEnum) {
			case ALL -> "";
			case DEPT -> {
				Long deptId = getUserDeptId(userId);
				if (deptId == null || deptId == 0) {
					yield "";
				}
				yield String.format(" AND %s.id = %d", deptAlias, deptId);
			}
			case DEPT_AND_CHILD -> {
				List<Long> deptIds = getDeptAndChildIds(userId);
				if (deptIds.isEmpty()) {
					yield "";
				}
				yield String.format(" AND %s.id IN (%s)", deptAlias,
						deptIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
			}
			case SELF -> String.format(" AND %s.id = %d", userAlias, userId);
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
