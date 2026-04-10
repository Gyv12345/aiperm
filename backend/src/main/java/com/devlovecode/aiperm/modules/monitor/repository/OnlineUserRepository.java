package com.devlovecode.aiperm.modules.monitor.repository;

import com.devlovecode.aiperm.common.repository.BaseJpaRepository;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.monitor.entity.SysOnlineUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnlineUserRepository extends BaseJpaRepository<SysOnlineUser> {

	Optional<SysOnlineUser> findByTokenAndDeleted(String token, Integer deleted);

	Optional<SysOnlineUser> findByIdAndDeleted(Long id, Integer deleted);

	List<SysOnlineUser> findAllByDeletedOrderByLastAccessTimeDesc(Integer deleted);

	long countByDeleted(Integer deleted);

	default Page<SysOnlineUser> queryPage(String username, String ip, Integer pageNum, Integer pageSize) {
		return findAll(
				SpecificationUtils.and(SpecificationUtils.like("username", username),
						SpecificationUtils.like("ip", ip)),
				PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "lastAccessTime")));
	}

}
