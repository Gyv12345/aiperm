package com.devlovecode.aiperm.modules.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSubmitDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalInstanceRepository;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalInstanceVO;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapter;
import com.devlovecode.aiperm.modules.im.adapter.ImPlatformAdapterFactory;
import com.devlovecode.aiperm.modules.notification.service.NotificationService;
import com.devlovecode.aiperm.modules.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.oauth.repository.UserOauthRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalSceneRepository sceneRepo;
    private final ApprovalInstanceRepository instanceRepo;
    private final UserOauthRepository userOauthRepo;
    private final ImPlatformAdapterFactory adapterFactory;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void submit(ApprovalSubmitDTO dto) {
        SysApprovalScene scene = sceneRepo.findBySceneCode(dto.getSceneCode())
                .orElse(null);
        if (scene == null || scene.getEnabled() == null || scene.getEnabled() != 1) {
            return;
        }

        if (instanceRepo.existsByBusinessTypeAndBusinessIdAndStatus(dto.getBusinessType(), dto.getBusinessId(), "PENDING")) {
            throw new BusinessException("该业务已有审批进行中");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        SysUserOauth oauth = userOauthRepo.findByUserIdAndPlatform(userId, scene.getPlatform())
                .orElseThrow(() -> new BusinessException("当前用户未绑定平台账号: " + scene.getPlatform()));

        ImPlatformAdapter adapter = adapterFactory.getAdapter(scene.getPlatform());
        String platformInstanceId = adapter.createApproval(scene, dto.getFormData(), oauth.getOpenId());

        SysApprovalInstance entity = new SysApprovalInstance();
        entity.setSceneCode(scene.getSceneCode());
        entity.setBusinessType(dto.getBusinessType());
        entity.setBusinessId(dto.getBusinessId());
        entity.setInitiatorId(userId);
        entity.setPlatform(scene.getPlatform());
        entity.setPlatformInstanceId(platformInstanceId);
        entity.setStatus("PENDING");
        entity.setFormData(writeJson(dto.getFormData()));
        entity.setCreateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        instanceRepo.save(entity);

        Map<String, Object> vars = new HashMap<>();
        vars.put("businessType", dto.getBusinessType());
        vars.put("businessId", dto.getBusinessId());
        notificationService.send("APPROVAL_SUBMIT", vars, java.util.List.of(userId));
    }

    public PageResult<ApprovalInstanceVO> queryMyInstances(String sceneCode, String status, Integer page, Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        Specification<SysApprovalInstance> spec = SpecificationUtils.and(
                SpecificationUtils.eq("initiatorId", userId),
                SpecificationUtils.eq("sceneCode", sceneCode),
                SpecificationUtils.eq("status", status)
        );
        PageRequest pageRequest = PageRequest.of((page == null ? 1 : page) - 1, pageSize == null ? 10 : pageSize);
        Page<SysApprovalInstance> jpaPage = instanceRepo.findAll(spec, pageRequest);
        return PageResult.fromJpaPage(jpaPage).map(this::toVO);
    }

    private ApprovalInstanceVO toVO(SysApprovalInstance entity) {
        ApprovalInstanceVO vo = new ApprovalInstanceVO();
        vo.setId(entity.getId());
        vo.setSceneCode(entity.getSceneCode());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setInitiatorId(entity.getInitiatorId());
        vo.setPlatform(entity.getPlatform());
        vo.setPlatformInstanceId(entity.getPlatformInstanceId());
        vo.setStatus(entity.getStatus());
        vo.setFormData(entity.getFormData());
        vo.setResultTime(entity.getResultTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String writeJson(Map<String, Object> map) {
        try {
            return map == null ? "{}" : objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
