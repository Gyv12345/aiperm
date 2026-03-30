package com.devlovecode.aiperm.modules.system.service;

import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("角色服务测试")
class RoleServiceTest {

	@Mock
	private RoleRepository roleRepo;

	@InjectMocks
	private RoleService roleService;

	@Test
	@DisplayName("更新角色时未传 dataScope，应保留原有值")
	void updateShouldKeepExistingDataScopeWhenOmitted() {
		Long roleId = 2L;
		SysRole entity = new SysRole();
		entity.setId(roleId);
		entity.setRoleName("old-name");
		entity.setRoleCode("old-code");
		entity.setDataScope(2);

		RoleDTO dto = new RoleDTO();
		dto.setRoleName("new-name");
		dto.setRoleCode("new-code");
		dto.setSort(1);
		dto.setStatus(0);
		dto.setRemark("remark");
		dto.setDataScope(null);

		when(roleRepo.findById(roleId)).thenReturn(Optional.of(entity));
		when(roleRepo.isBuiltin(roleId)).thenReturn(false);
		when(roleRepo.existsByRoleCodeExcludeId(dto.getRoleCode(), roleId)).thenReturn(false);

		roleService.update(roleId, dto);

		assertEquals(2, entity.getDataScope());
		verify(roleRepo).save(entity);
	}

	@Test
	@DisplayName("更新角色时传入 dataScope，应覆盖原有值")
	void updateShouldOverwriteDataScopeWhenProvided() {
		Long roleId = 2L;
		SysRole entity = new SysRole();
		entity.setId(roleId);
		entity.setRoleName("old-name");
		entity.setRoleCode("old-code");
		entity.setDataScope(2);

		RoleDTO dto = new RoleDTO();
		dto.setRoleName("new-name");
		dto.setRoleCode("new-code");
		dto.setSort(1);
		dto.setStatus(0);
		dto.setRemark("remark");
		dto.setDataScope(4);

		when(roleRepo.findById(roleId)).thenReturn(Optional.of(entity));
		when(roleRepo.isBuiltin(roleId)).thenReturn(false);
		when(roleRepo.existsByRoleCodeExcludeId(dto.getRoleCode(), roleId)).thenReturn(false);

		roleService.update(roleId, dto);

		assertEquals(4, entity.getDataScope());
		verify(roleRepo).save(entity);
	}

}
