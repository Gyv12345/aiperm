package com.devlovecode.aiperm.common.repository;

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

}
