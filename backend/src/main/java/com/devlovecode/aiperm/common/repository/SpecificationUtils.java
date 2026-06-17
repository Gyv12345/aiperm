package com.devlovecode.aiperm.common.repository;

import com.devlovecode.aiperm.common.context.DataScopeContext;
import com.devlovecode.aiperm.common.context.DataScopeHolder;
import com.devlovecode.aiperm.common.enums.DataScopeEnum;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Specification 工具类 替代 SqlBuilder，提供动态查询条件构建
 */
public final class SpecificationUtils {

	private SpecificationUtils() {
	}

	/**
	 * LIKE 模糊查询
	 */
	public static <T> Specification<T> like(String fieldName, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return (root, query, cb) -> cb.like(root.get(fieldName), "%" + value + "%");
	}

	/**
	 * 精确匹配
	 */
	public static <T> Specification<T> eq(String fieldName, Object value) {
		if (value == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get(fieldName), value);
	}

	/**
	 * 大于等于
	 */
	public static <T, V extends Comparable<? super V>> Specification<T> ge(String fieldName, V value) {
		if (value == null) {
			return null;
		}
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(fieldName), value);
	}

	/**
	 * 小于等于
	 */
	public static <T, V extends Comparable<? super V>> Specification<T> le(String fieldName, V value) {
		if (value == null) {
			return null;
		}
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(fieldName), value);
	}

	/**
	 * IN 查询
	 */
	public static <T> Specification<T> in(String fieldName, Collection<?> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return (root, query, cb) -> root.get(fieldName).in(values);
	}

	/**
	 * 组合多个条件（AND）
	 */
	@SafeVarargs
	public static <T> Specification<T> and(Specification<T>... specs) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			for (Specification<T> spec : specs) {
				if (spec != null) {
					Predicate p = spec.toPredicate(root, query, cb);
					if (p != null) {
						predicates.add(p);
					}
				}
			}
			return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	/**
	 * 数据权限条件：从 {@link DataScopeHolder} 读取当前请求的数据权限上下文，
	 * 按范围生成对应的 Criteria 谓词。
	 *
	 * <p>用法（在 Service 的 SpecificationUtils.and(...) 参数列表追加）：
	 * <pre>{@code
	 *   SpecificationUtils.and(
	 *       SpecificationUtils.like("title", dto.getTitle()),
	 *       SpecificationUtils.dataScope("deptId", "createBy")  // 追加数据权限
	 *   );
	 * }</pre>
	 *
	 * @param deptField    部门外键字段名（用于 DEPT / DEPT_AND_CHILD），如 "deptId"；
	 *                     若实体无部门字段（如按创建人过滤的业务），传 null
	 * @param creatorField 创建人字段名（用于 SELF，通常是 BaseEntity 的 "createBy"）；
	 *                     若实体按自身主键过滤（如 SysUser），传 "id"
	 * @return 数据权限 Specification；ALL 或超管时返回 null（被 and() 自动跳过）
	 */
	public static <T> Specification<T> dataScope(String deptField, String creatorField) {
		DataScopeContext ctx = DataScopeHolder.get();
		if (ctx.noFilter()) {
			return null;
		}
		return (root, query, cb) -> {
			return switch (ctx.scope()) {
				case DEPT -> (deptField == null || ctx.deptId() == null)
						? null
						: cb.equal(root.get(deptField), ctx.deptId());
				case DEPT_AND_CHILD -> (deptField == null || ctx.deptIds() == null || ctx.deptIds().isEmpty())
						? null
						: root.get(deptField).in(ctx.deptIds());
				case SELF -> (creatorField == null || ctx.userId() == null)
						? null
						: cb.equal(root.get(creatorField), String.valueOf(ctx.userId()));
				default -> null;
			};
		};
	}

}
