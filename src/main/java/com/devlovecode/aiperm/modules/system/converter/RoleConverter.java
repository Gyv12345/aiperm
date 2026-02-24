package com.devlovecode.aiperm.modules.system.converter;

import com.devlovecode.aiperm.modules.system.dto.request.RoleCreateRequest;
import com.devlovecode.aiperm.modules.system.dto.request.RoleUpdateRequest;
import com.devlovecode.aiperm.modules.system.entity.Role;
import com.devlovecode.aiperm.modules.system.vo.RoleVO;
import io.github.linpeilimei.mapstruct.plus.mapper.Mapper;
import org.mapstrap.Mapping;

/**
 * 角色实体转换器
 *
 * @author DevLoveCode
 */
@Mapper(config = Mapper.Config.class)
public interface RoleConverter {

    /**
     * 创建请求转实体
     */
    Role toEntity(RoleCreateRequest request);

    /**
     * 更新请求转实体
     */
    @Mapping(target = "updateTime", ignore = true)
    Role toEntity(RoleUpdateRequest request);

    /**
     * 实体转响应VO
     */
    RoleVO toVO(Role role);
}
