package com.devlovecode.aiperm.modules.system.converter;

import com.devlovecode.aiperm.modules.system.dto.request.UserCreateRequest;
import com.devlovecode.aiperm.modules.system.dto.request.UserUpdateRequest;
import com.devlovecode.aiperm.modules.system.entity.User;
import com.devlovecode.aiperm.modules.system.vo.UserVO;
import io.github.linpeilimei.mapstruct.plus.mapper.Mapper;
import org.mapstrap.Mapping;

/**
 * 用户实体转换器
 *
 * @author DevLoveCode
 */
@Mapper(config = Mapper.Config.class)
public interface UserConverter {

    /**
     * 创建请求转实体
     */
    User toEntity(UserCreateRequest request);

    /**
     * 更新请求转实体
     */
    @Mapping(target = "updateTime", ignore = true)
    User toEntity(UserUpdateRequest request);

    /**
     * 实体转响应VO
     */
    UserVO toVO(User user);
}
