package com.devlovecode.aiperm.modules.approval.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.common.repository.SpecificationUtils;
import com.devlovecode.aiperm.modules.approval.adapter.ApprovalCallbackPayload;
import com.devlovecode.aiperm.modules.approval.adapter.ImApprovalAdapter;
import com.devlovecode.aiperm.modules.approval.api.ApprovalClient;
import com.devlovecode.aiperm.modules.approval.api.ApprovalHandler;
import com.devlovecode.aiperm.modules.approval.api.ApprovalSubmitCommand;
import com.devlovecode.aiperm.modules.approval.api.ApprovalTaskContext;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalInstanceDTO;
import com.devlovecode.aiperm.modules.approval.dto.ApprovalSubmitDTO;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalCallbackLog;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalInstance;
import com.devlovecode.aiperm.modules.approval.entity.SysApprovalScene;
import com.devlovecode.aiperm.modules.approval.entity.SysImConfig;
import com.devlovecode.aiperm.modules.approval.entity.SysMessageLogRecord;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalCallbackLogRepository;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalInstanceRepository;
import com.devlovecode.aiperm.modules.approval.repository.ApprovalSceneRepository;
import com.devlovecode.aiperm.modules.approval.repository.MessageLogRecordRepository;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalInstanceVO;
import com.devlovecode.aiperm.modules.approval.vo.ApprovalTodoOverviewVO;
import com.devlovecode.aiperm.modules.auth.oauth.entity.SysUserOauth;
import com.devlovecode.aiperm.modules.auth.oauth.repository.UserOauthRepository;
import com.devlovecode.aiperm.modules.system.api.SystemAccess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalBridgeService implements ApprovalClient {

	private static final String STATUS_PENDING = "PENDING";

	private static final String STATUS_APPROVED = "APPROVED";

	private static final String STATUS_REJECTED = "REJECTED";

	private static final String STATUS_CANCELED = "CANCELED";

	private static final List<String> DIAGNOSTIC_PERMISSIONS = List.of("system:im-config:list",
			"system:approval-scene:list", "enterprise:message-log:list");

	private final ApprovalSceneRepository approvalSceneRepo;

	private final ApprovalInstanceRepository approvalInstanceRepo;

	private final ApprovalCallbackLogRepository approvalCallbackLogRepo;

	private final MessageLogRecordRepository messageLogRecordRepo;

	private final UserOauthRepository userOauthRepo;

	private final ImConfigService imConfigService;

	private final SystemAccess systemAccess;

	private final List<ImApprovalAdapter> adapters;

	private final ObjectMapper objectMapper;

	private final ApplicationContext applicationContext;

	private Map<String, ImApprovalAdapter> adapterMap;

	@PostConstruct
	public void init() {
		adapterMap = adapters.stream().collect(Collectors.toMap(adapter -> adapter.platform().toUpperCase(),
				Function.identity(), (left, right) -> left, LinkedHashMap::new));
	}

	@Override
	@Transactional
	public Long submit(ApprovalSubmitCommand command) {
		if (!isApprovalModuleEnabled()) {
			return unavailable(command.required(), "审批模块未启用，请先开启配置键 approval.module.enabled");
		}

		SysApprovalScene scene = approvalSceneRepo.findBySceneCode(command.sceneCode()).orElse(null);
		if (scene == null) {
			return unavailable(command.required(), "审批场景不存在：" + command.sceneCode());
		}
		if (!isEnabled(scene.getEnabled()) || !isEnabled(scene.getAutoSubmitEnabled())) {
			return unavailable(command.required(), "审批场景未启用或未开启自动提交：" + scene.getSceneCode());
		}

		SysImConfig config = imConfigService.getOrCreateEntity(scene.getPlatform());
		if (!isEnabled(config.getEnabled())) {
			return unavailable(command.required(), "管理员尚未启用 " + scene.getPlatform() + " 审批通道");
		}
		ImConfigService.ConfigAssessment assessment = imConfigService.assessConfig(config);
		if (!assessment.ready()) {
			return unavailable(command.required(),
					scene.getPlatform() + " 平台配置不完整，缺少字段: " + String.join(", ", assessment.missingFields()));
		}

		Long userId = getCurrentUserId();
		SysUserOauth binding = userOauthRepo.findByUserIdAndPlatform(userId, scene.getPlatform()).filter(
				item -> Integer.valueOf(1).equals(item.getStatus())).orElse(null);
		if (binding == null) {
			return unavailable(command.required(), "当前账号未绑定 " + scene.getPlatform() + " 账号");
		}

		String businessType = isBlank(command.businessType()) ? scene.getBusinessType() : command.businessType().trim();
		String activeInstanceKey = buildActiveInstanceKey(businessType, command.businessId());
		if (!isEnabled(scene.getAllowDuplicatePending()) && approvalInstanceRepo.findByActiveInstanceKey(activeInstanceKey).isPresent()) {
			throw new BusinessException("该业务已有审批进行中");
		}

		ImApprovalAdapter adapter = getAdapter(scene.getPlatform());
		String platformInstanceId = adapter.createApproval(config, scene, binding, safePayload(command.payload()));
		SysApprovalInstance instance = new SysApprovalInstance();
		instance.setSceneCode(scene.getSceneCode());
		instance.setBusinessType(businessType);
		instance.setBusinessId(command.businessId());
		instance.setInitiatorId(userId);
		instance.setInitiatorName(resolveCurrentUserDisplayName());
		instance.setPlatform(scene.getPlatform());
		instance.setPlatformInstanceId(platformInstanceId);
		instance.setStatus(STATUS_PENDING);
		instance.setFormData(writePayload(safePayload(command.payload())));
		instance.setLastSyncTime(LocalDateTime.now());
		instance.setActiveInstanceKey(isEnabled(scene.getAllowDuplicatePending()) ? null : activeInstanceKey);
		instance.setCreateBy(getCurrentUsername());
		instance.setCreateTime(LocalDateTime.now());
		approvalInstanceRepo.save(instance);
		return instance.getId();
	}

	@Override
	public Optional<ApprovalTaskContext> queryLatest(String businessType, Long businessId) {
		return approvalInstanceRepo.findFirstByBusinessTypeAndBusinessIdOrderByCreateTimeDesc(businessType, businessId)
			.map(instance -> toTaskContext(instance, findScene(instance.getSceneCode()).orElse(null)));
	}

	@Override
	@Transactional
	public void cancel(String sceneCode, String businessType, Long businessId, String reason) {
		SysApprovalScene scene = approvalSceneRepo.findBySceneCode(sceneCode)
			.orElseThrow(() -> new BusinessException("审批场景不存在：" + sceneCode));
		String resolvedBusinessType = isBlank(businessType) ? scene.getBusinessType() : businessType.trim();
		SysApprovalInstance instance = approvalInstanceRepo
			.findFirstBySceneCodeAndBusinessTypeAndBusinessIdOrderByCreateTimeDesc(sceneCode, resolvedBusinessType,
					businessId)
			.orElseThrow(() -> new BusinessException("审批实例不存在"));
		if (!STATUS_PENDING.equals(instance.getStatus())) {
			throw new BusinessException("仅进行中的审批支持取消");
		}

		instance.setStatus(STATUS_CANCELED);
		instance.setErrorMessage(reason);
		instance.setResultTime(LocalDateTime.now());
		instance.setLastSyncTime(LocalDateTime.now());
		instance.setActiveInstanceKey(null);
		instance.setUpdateBy(getCurrentUsername());
		instance.setUpdateTime(LocalDateTime.now());
		approvalInstanceRepo.save(instance);
		dispatchHandler(scene, STATUS_CANCELED, instance);
	}

	@Transactional
	public Long submit(ApprovalSubmitDTO dto) {
		return submit(new ApprovalSubmitCommand(dto.getSceneCode(), dto.getBusinessType(), dto.getBusinessId(),
				safePayload(dto.getPayload()), Boolean.TRUE.equals(dto.getRequired())));
	}

	public PageResult<ApprovalInstanceVO> queryMyPage(ApprovalInstanceDTO dto) {
		Specification<SysApprovalInstance> spec = SpecificationUtils.and(
				SpecificationUtils.eq("initiatorId", getCurrentUserId()),
				SpecificationUtils.like("sceneCode", dto.getSceneCode()),
				SpecificationUtils.like("businessType", dto.getBusinessType()),
				SpecificationUtils.eq("platform", normalizePlatform(dto.getPlatform())),
				SpecificationUtils.eq("status", normalizeStatus(dto.getStatus())));
		PageRequest pageRequest = PageRequest.of(dto.getPage() - 1, dto.getPageSize(),
				Sort.by(Sort.Direction.DESC, "createTime"));
		Page<SysApprovalInstance> page = approvalInstanceRepo.findAll(spec, pageRequest);
		return PageResult.fromJpaPage(page).map(this::toInstanceVO);
	}

	@Transactional
	public String handleCallback(String platform, String body, Map<String, String> headers) {
		String normalizedPlatform = normalizePlatform(platform);
		SysImConfig config = imConfigService.getOrCreateEntity(normalizedPlatform);
		ImApprovalAdapter adapter = getAdapter(normalizedPlatform);
		ApprovalCallbackPayload callback = adapter.parseCallback(config, body == null ? "" : body, headers);
		LocalDateTime now = LocalDateTime.now();

		Optional<SysApprovalInstance> optionalInstance = approvalInstanceRepo.findByPlatformInstanceId(callback.platformInstanceId());
		if (optionalInstance.isEmpty()) {
			saveCallbackLog(normalizedPlatform, callback.sceneCode(), callback.platformInstanceId(), callback.status(),
					"IGNORED", "审批实例不存在", callback.rawPayload(), now);
			return "success";
		}

		SysApprovalInstance instance = optionalInstance.get();
		SysApprovalScene scene = findScene(instance.getSceneCode())
			.orElseThrow(() -> new BusinessException("审批场景不存在：" + instance.getSceneCode()));
		String normalizedStatus = normalizeStatus(callback.status());

		if (isTerminal(instance.getStatus()) && instance.getStatus().equals(normalizedStatus) && isBlank(instance.getErrorMessage())) {
			saveCallbackLog(normalizedPlatform, scene.getSceneCode(), instance.getPlatformInstanceId(), normalizedStatus,
					"IGNORED", "重复回调已忽略", callback.rawPayload(), now);
			return "success";
		}

		instance.setStatus(normalizedStatus);
		instance.setCallbackRaw(callback.rawPayload());
		instance.setLastSyncTime(now);
		if (isTerminal(normalizedStatus)) {
			instance.setResultTime(now);
			instance.setActiveInstanceKey(null);
		}

		try {
			dispatchHandler(scene, normalizedStatus, instance);
			instance.setErrorMessage(null);
			instance.setUpdateTime(now);
			instance.setUpdateBy("callback:" + normalizedPlatform.toLowerCase());
			approvalInstanceRepo.save(instance);
			saveCallbackLog(normalizedPlatform, scene.getSceneCode(), instance.getPlatformInstanceId(), normalizedStatus,
					"SUCCESS", null, callback.rawPayload(), now);
			return "success";
		}
		catch (Exception e) {
			instance.setErrorMessage(truncate(e.getMessage(), 500));
			instance.setUpdateTime(now);
			instance.setUpdateBy("callback:" + normalizedPlatform.toLowerCase());
			approvalInstanceRepo.save(instance);
			saveCallbackLog(normalizedPlatform, scene.getSceneCode(), instance.getPlatformInstanceId(), normalizedStatus,
					"FAILED", truncate(e.getMessage(), 500), callback.rawPayload(), now);
			return "fail";
		}
	}

	public ApprovalTodoOverviewVO getTodoOverview(String platform) {
		String focusPlatform = isBlank(platform) ? "FEISHU" : normalizePlatform(platform);
		Long userId = getCurrentUserId();
		boolean moduleEnabled = isApprovalModuleEnabled();
		SysImConfig config = imConfigService.getOrCreateEntity(focusPlatform);
		ImConfigService.ConfigAssessment assessment = imConfigService.assessConfig(config);
		boolean oauthBound = userOauthRepo.findByUserIdAndPlatform(userId, focusPlatform)
			.filter(item -> Integer.valueOf(1).equals(item.getStatus()))
			.isPresent();
		long enabledSceneCount = approvalSceneRepo.countByPlatformAndEnabledAndDeleted(focusPlatform, 1, 0);
		String todoUrl = getAdapter(focusPlatform).resolveTodoUrl(config);
		boolean diagnosticsVisible = hasDiagnosticAccess(userId);

		ApprovalTodoOverviewVO overview = new ApprovalTodoOverviewVO();
		overview.setViewer(buildViewer(userId, diagnosticsVisible, focusPlatform));
		overview.setUserGuide(buildUserGuide(moduleEnabled, config, assessment.ready(), oauthBound, enabledSceneCount, todoUrl));
		overview.setQuickActions(buildQuickActions(moduleEnabled, focusPlatform, config, oauthBound, enabledSceneCount, todoUrl));
		if (diagnosticsVisible) {
			overview.setAdminDiagnostics(buildAdminDiagnostics(focusPlatform, config, assessment, enabledSceneCount));
		}
		return overview;
	}

	private ApprovalTodoOverviewVO.Viewer buildViewer(Long userId, boolean diagnosticsVisible, String focusPlatform) {
		ApprovalTodoOverviewVO.Viewer viewer = new ApprovalTodoOverviewVO.Viewer();
		viewer.setUserId(userId);
		viewer.setIsAdmin(diagnosticsVisible);
		viewer.setFocusPlatform(focusPlatform);
		return viewer;
	}

	private ApprovalTodoOverviewVO.UserGuide buildUserGuide(boolean moduleEnabled, SysImConfig config,
			boolean platformConfigReady, boolean oauthBound, long enabledSceneCount, String todoUrl) {
		ApprovalTodoOverviewVO.UserGuide userGuide = new ApprovalTodoOverviewVO.UserGuide();
		userGuide.setModuleEnabled(moduleEnabled);
		boolean platformEnabled = isEnabled(config.getEnabled());
		userGuide.setPlatformEnabled(platformEnabled);
		userGuide.setPlatformConfigReady(platformConfigReady);
		userGuide.setOauthBound(oauthBound);
		userGuide.setEnabledSceneCount(enabledSceneCount);
		if (!moduleEnabled) {
			userGuide.setNextStep("ENABLE_APPROVAL_MODULE");
			userGuide.setNextStepReason("审批模块未启用，请先在系统配置中开启 approval.module.enabled。");
		}
		else if (!platformEnabled) {
			userGuide.setNextStep("ENABLE_PLATFORM");
			userGuide.setNextStepReason("管理员尚未启用 " + config.getPlatform() + " 审批通道。");
		}
		else if (!platformConfigReady) {
			userGuide.setNextStep("CONFIGURE_PLATFORM");
			userGuide.setNextStepReason(config.getPlatform() + " 平台配置尚不完整，请补齐 App ID、Secret、回调密钥等字段。");
		}
		else if (!oauthBound) {
			userGuide.setNextStep("BIND_OAUTH");
			userGuide.setNextStepReason("当前账号未绑定 " + config.getPlatform() + "。");
		}
		else if (enabledSceneCount <= 0) {
			userGuide.setNextStep("CONFIGURE_SCENE");
			userGuide.setNextStepReason(config.getPlatform() + " 通道已开通，但尚未配置可用审批场景。");
		}
		else if (isBlank(todoUrl)) {
			userGuide.setNextStep("CONFIGURE_TODO_URL");
			userGuide.setNextStepReason("已具备审批条件，但尚未配置平台待办地址。");
		}
		else {
			userGuide.setNextStep("OPEN_PLATFORM_TODO");
			userGuide.setNextStepReason("可以前往平台处理待办。");
		}
		return userGuide;
	}

	private List<ApprovalTodoOverviewVO.QuickAction> buildQuickActions(boolean moduleEnabled, String platform,
			SysImConfig config, boolean oauthBound, long enabledSceneCount, String todoUrl) {
		boolean platformEnabled = isEnabled(config.getEnabled());
		ApprovalTodoOverviewVO.QuickAction openTodo = new ApprovalTodoOverviewVO.QuickAction();
		openTodo.setCode("OPEN_PLATFORM_TODO");
		openTodo.setEnabled(moduleEnabled && platformEnabled && oauthBound && enabledSceneCount > 0 && !isBlank(todoUrl));
		openTodo.setUrl(todoUrl);
		openTodo.setReason(resolveTodoActionReason(moduleEnabled, platformEnabled, oauthBound, enabledSceneCount, todoUrl,
				platform));

		ApprovalTodoOverviewVO.QuickAction bindOauth = new ApprovalTodoOverviewVO.QuickAction();
		bindOauth.setCode("BIND_OAUTH");
		bindOauth.setEnabled(moduleEnabled && !oauthBound);
		bindOauth.setUrl("/oauth/bind/" + platform);
		bindOauth.setReason(!moduleEnabled ? "审批模块未启用" : oauthBound ? "当前账号已绑定" + platform : "绑定后即可处理平台待办");

		ApprovalTodoOverviewVO.QuickAction viewMyApproval = new ApprovalTodoOverviewVO.QuickAction();
		viewMyApproval.setCode("VIEW_MY_APPROVAL");
		viewMyApproval.setEnabled(true);
		viewMyApproval.setUrl("/approval/my");
		viewMyApproval.setReason("查看我发起的审批");

		return List.of(openTodo, bindOauth, viewMyApproval);
	}

	private ApprovalTodoOverviewVO.AdminDiagnostics buildAdminDiagnostics(String platform, SysImConfig config,
			ImConfigService.ConfigAssessment assessment, long enabledSceneCount) {
		ApprovalTodoOverviewVO.AdminDiagnostics diagnostics = new ApprovalTodoOverviewVO.AdminDiagnostics();

		ApprovalTodoOverviewVO.PlatformCheck platformCheck = new ApprovalTodoOverviewVO.PlatformCheck();
		platformCheck.setPlatform(platform);
		platformCheck.setEnabled(isEnabled(config.getEnabled()));
		platformCheck.setConfigReady(assessment.ready());
		platformCheck.setMissingFields(assessment.missingFields());
		diagnostics.setPlatformChecks(List.of(platformCheck));

		ApprovalTodoOverviewVO.SceneCheck sceneCheck = new ApprovalTodoOverviewVO.SceneCheck();
		sceneCheck.setPlatform(platform);
		sceneCheck.setEnabledSceneCount(enabledSceneCount);
		sceneCheck.setSampleSceneCode(approvalSceneRepo.findFirstByPlatformAndEnabledAndDeletedOrderByIdAsc(platform, 1, 0)
			.map(SysApprovalScene::getSceneCode)
			.orElse(null));
		diagnostics.setSceneChecks(List.of(sceneCheck));

		diagnostics.setLatestApprovalCallback(approvalCallbackLogRepo.findFirstByPlatformOrderByProcessedTimeDescCreateTimeDesc(platform)
			.map(this::toCallbackSummary)
			.orElse(null));
		diagnostics.setLatestMessagePush(messageLogRecordRepo.findFirstByPlatformOrderBySendTimeDescCreateTimeDesc(platform)
			.map(this::toMessagePushSummary)
			.orElse(null));
		return diagnostics;
	}

	private ApprovalTodoOverviewVO.CallbackSummary toCallbackSummary(SysApprovalCallbackLog entity) {
		ApprovalTodoOverviewVO.CallbackSummary summary = new ApprovalTodoOverviewVO.CallbackSummary();
		summary.setPlatform(entity.getPlatform());
		summary.setSceneCode(entity.getSceneCode());
		summary.setPlatformInstanceId(entity.getPlatformInstanceId());
		summary.setStatus(entity.getCallbackStatus());
		summary.setResultTime(entity.getProcessedTime());
		return summary;
	}

	private ApprovalTodoOverviewVO.MessagePushSummary toMessagePushSummary(SysMessageLogRecord entity) {
		ApprovalTodoOverviewVO.MessagePushSummary summary = new ApprovalTodoOverviewVO.MessagePushSummary();
		summary.setPlatform(entity.getPlatform());
		summary.setTemplateCode(entity.getTemplateCode());
		summary.setStatus(entity.getStatus());
		summary.setErrorMsg(entity.getErrorMsg());
		summary.setSendTime(entity.getSendTime() != null ? entity.getSendTime() : entity.getCreateTime());
		return summary;
	}

	private void dispatchHandler(SysApprovalScene scene, String status, SysApprovalInstance instance) {
		if (!isTerminal(status)) {
			return;
		}
		if (isBlank(scene.getHandlerBeanName())) {
			throw new BusinessException("审批场景未配置处理器");
		}
		Object bean = applicationContext.getBean(scene.getHandlerBeanName());
		if (!(bean instanceof ApprovalHandler handler)) {
			throw new BusinessException("审批处理器不是 ApprovalHandler 类型：" + scene.getHandlerBeanName());
		}

		ApprovalTaskContext context = toTaskContext(instance, scene);
		switch (status) {
			case STATUS_APPROVED -> handler.onApproved(context);
			case STATUS_REJECTED -> handler.onRejected(context);
			case STATUS_CANCELED -> handler.onCanceled(context);
			default -> {
			}
		}
	}

	private ApprovalTaskContext toTaskContext(SysApprovalInstance instance, SysApprovalScene scene) {
		return new ApprovalTaskContext(instance.getId(), instance.getSceneCode(), scene != null ? scene.getSceneName() : null,
				instance.getBusinessType(), instance.getBusinessId(), instance.getInitiatorId(), instance.getInitiatorName(),
				instance.getPlatform(), instance.getPlatformInstanceId(), instance.getStatus(),
				readPayload(instance.getFormData()), instance.getCreateTime(), instance.getResultTime());
	}

	private ApprovalInstanceVO toInstanceVO(SysApprovalInstance entity) {
		ApprovalInstanceVO vo = new ApprovalInstanceVO();
		vo.setId(entity.getId());
		vo.setSceneCode(entity.getSceneCode());
		vo.setSceneName(findScene(entity.getSceneCode()).map(SysApprovalScene::getSceneName).orElse(entity.getSceneCode()));
		vo.setBusinessType(entity.getBusinessType());
		vo.setBusinessId(entity.getBusinessId());
		vo.setInitiatorId(entity.getInitiatorId());
		vo.setInitiatorName(entity.getInitiatorName());
		vo.setPlatform(entity.getPlatform());
		vo.setPlatformInstanceId(entity.getPlatformInstanceId());
		vo.setStatus(entity.getStatus());
		vo.setErrorMessage(entity.getErrorMessage());
		vo.setCreateTime(entity.getCreateTime());
		vo.setResultTime(entity.getResultTime());
		return vo;
	}

	private Optional<SysApprovalScene> findScene(String sceneCode) {
		return approvalSceneRepo.findBySceneCode(sceneCode);
	}

	private void saveCallbackLog(String platform, String sceneCode, String platformInstanceId, String status,
			String handleResult, String errorMessage, String payload, LocalDateTime processedTime) {
		SysApprovalCallbackLog log = new SysApprovalCallbackLog();
		log.setPlatform(platform);
		log.setSceneCode(sceneCode);
		log.setPlatformInstanceId(platformInstanceId);
		log.setCallbackStatus(status);
		log.setHandleResult(handleResult);
		log.setErrorMessage(errorMessage);
		log.setPayload(payload);
		log.setProcessedTime(processedTime);
		log.setCreateBy("system");
		log.setCreateTime(processedTime);
		approvalCallbackLogRepo.save(log);
	}

	private ImApprovalAdapter getAdapter(String platform) {
		ImApprovalAdapter adapter = adapterMap.get(normalizePlatform(platform));
		if (adapter == null) {
			throw new BusinessException("暂不支持的审批平台：" + platform);
		}
		return adapter;
	}

	private Long getCurrentUserId() {
		try {
			return StpUtil.getLoginIdAsLong();
		}
		catch (Exception e) {
			throw new BusinessException("请先登录");
		}
	}

	private String getCurrentUsername() {
		try {
			return StpUtil.getLoginIdAsString();
		}
		catch (Exception e) {
			return "system";
		}
	}

	private String resolveCurrentUserDisplayName() {
		return systemAccess.findUserById(getCurrentUserId())
			.map(user -> {
				if (!isBlank(user.nickname())) {
					return user.nickname();
				}
				return user.username();
			})
			.orElse(getCurrentUsername());
	}

	private boolean hasDiagnosticAccess(Long userId) {
		if (systemAccess.isAdmin(userId)) {
			return true;
		}
		List<String> permissions = systemAccess.getUserPermissions(userId);
		return permissions.stream().anyMatch(DIAGNOSTIC_PERMISSIONS::contains);
	}

	private String buildActiveInstanceKey(String businessType, Long businessId) {
		return businessType + ":" + businessId;
	}

	private boolean isTerminal(String status) {
		return STATUS_APPROVED.equals(status) || STATUS_REJECTED.equals(status) || STATUS_CANCELED.equals(status);
	}

	private boolean isEnabled(Integer value) {
		return Integer.valueOf(1).equals(value);
	}

	private boolean isApprovalModuleEnabled() {
		return systemAccess.getBooleanConfig("approval.module.enabled", false);
	}

	private String normalizePlatform(String platform) {
		return platform == null ? null : platform.trim().toUpperCase();
	}

	private String normalizeStatus(String status) {
		return status == null ? null : status.trim().toUpperCase();
	}

	private Map<String, Object> safePayload(Map<String, Object> payload) {
		return payload == null ? Collections.emptyMap() : payload;
	}

	private String writePayload(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(safePayload(payload));
		}
		catch (Exception e) {
			throw new BusinessException("审批载荷序列化失败");
		}
	}

	private Map<String, Object> readPayload(String payload) {
		try {
			if (isBlank(payload)) {
				return Collections.emptyMap();
			}
			return objectMapper.readValue(payload, new TypeReference<>() {
			});
		}
		catch (Exception e) {
			return Collections.emptyMap();
		}
	}

	private String resolveTodoActionReason(boolean moduleEnabled, boolean platformEnabled, boolean oauthBound,
			long enabledSceneCount, String todoUrl, String platform) {
		if (!moduleEnabled) {
			return "审批模块未启用";
		}
		if (!platformEnabled) {
			return "管理员尚未启用" + platform + "审批通道";
		}
		if (!oauthBound) {
			return "未绑定" + platform + "账号";
		}
		if (enabledSceneCount <= 0) {
			return "尚未配置可用审批场景";
		}
		if (isBlank(todoUrl)) {
			return "尚未配置平台待办地址";
		}
		return "前往平台处理审批待办";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String truncate(String value, int limit) {
		if (value == null || value.length() <= limit) {
			return value;
		}
		return value.substring(0, limit);
	}

	private Long unavailable(boolean required, String message) {
		if (required) {
			throw new BusinessException(message);
		}
		return null;
	}

}
