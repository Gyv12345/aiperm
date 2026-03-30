package com.devlovecode.aiperm.common.repository;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository 基础接口 提供软删除和批量软删除，禁止硬删除
 *
 * @param <T> 实体类型，必须继承 BaseEntity
 */
@NoRepositoryBean
public interface BaseJpaRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

	/**
	 * 软删除
	 */
	@Modifying
	@Query("UPDATE #{#entityName} e SET e.deleted = 1, e.updateTime = :updateTime WHERE e.id = :id")
	int softDelete(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 批量软删除
	 */
	@Modifying
	@Query("UPDATE #{#entityName} e SET e.deleted = 1, e.updateTime = :updateTime WHERE e.id IN :ids")
	int softDeleteByIds(@Param("ids") List<Long> ids, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 禁止硬删除，强制使用软删除
	 */
	@Override
	default void deleteById(Long id) {
		throw new UnsupportedOperationException("请使用 softDelete() 进行软删除");
	}

	@Override
	default void delete(T entity) {
		throw new UnsupportedOperationException("请使用 softDelete() 进行软删除");
	}

	@Override
	default void deleteAllById(Iterable<? extends Long> ids) {
		throw new UnsupportedOperationException("请使用 softDeleteByIds() 进行软删除");
	}

	@Override
	default void deleteAll(Iterable<? extends T> entities) {
		throw new UnsupportedOperationException("请使用 softDeleteByIds() 进行软删除");
	}

	@Override
	default void deleteAll() {
		throw new UnsupportedOperationException("请使用 softDeleteByIds() 进行软删除");
	}

}
