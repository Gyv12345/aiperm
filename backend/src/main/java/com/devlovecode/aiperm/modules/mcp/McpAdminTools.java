package com.devlovecode.aiperm.modules.mcp;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.modules.audit.service.LoginLogService;
import com.devlovecode.aiperm.modules.audit.service.OperLogService;
import com.devlovecode.aiperm.modules.dashboard.service.DashboardService;
import com.devlovecode.aiperm.modules.dashboard.vo.DashboardStatsVO;
import com.devlovecode.aiperm.modules.enterprise.dto.JobDTO;
import com.devlovecode.aiperm.modules.enterprise.service.JobService;
import com.devlovecode.aiperm.modules.enterprise.service.NoticeService;
import com.devlovecode.aiperm.modules.monitor.service.MonitorServerService;
import com.devlovecode.aiperm.modules.monitor.service.OnlineUserService;
import com.devlovecode.aiperm.modules.system.config.dto.ConfigDTO;
import com.devlovecode.aiperm.modules.system.config.service.ConfigService;
import com.devlovecode.aiperm.modules.system.rbac.dto.UserDTO;
import com.devlovecode.aiperm.modules.system.rbac.service.DeptService;
import com.devlovecode.aiperm.modules.system.rbac.service.DictDataService;
import com.devlovecode.aiperm.modules.system.rbac.service.DictTypeService;
import com.devlovecode.aiperm.modules.system.rbac.service.RoleService;
import com.devlovecode.aiperm.modules.system.rbac.service.UserService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP 管理端工具集（只读）。
 *
 * <p>把后台的查询能力作为 MCP 工具暴露给 AI 客户端（Claude Code / Cursor 等）。
 * 持有效 API Key 的客户端等同「管理员只读」视角，因此这里刻意只开放查询类操作，
 * 不暴露任何增删改能力；写操作请走管理界面（登录态 + 按钮权限 + 操作日志）。
 *
 * <p>端点与鉴权：/mcp（Streamable HTTP）+ X-API-Key，见 McpAuthFilterConfig；
 * 新增工具只需在本类加 @McpTool 方法，启动时自动注册。
 */
@Component
public class McpAdminTools {

	private final DashboardService dashboardService;

	private final UserService userService;

	private final RoleService roleService;

	private final DeptService deptService;

	private final DictTypeService dictTypeService;

	private final DictDataService dictDataService;

	private final LoginLogService loginLogService;

	private final OperLogService operLogService;

	private final OnlineUserService onlineUserService;

	private final MonitorServerService monitorServerService;

	private final NoticeService noticeService;

	private final JobService jobService;

	private final ConfigService configService;

	public McpAdminTools(DashboardService dashboardService, UserService userService, RoleService roleService,
			DeptService deptService, DictTypeService dictTypeService, DictDataService dictDataService,
			LoginLogService loginLogService, OperLogService operLogService, OnlineUserService onlineUserService,
			MonitorServerService monitorServerService, NoticeService noticeService, JobService jobService,
			ConfigService configService) {
		this.dashboardService = dashboardService;
		this.userService = userService;
		this.roleService = roleService;
		this.deptService = deptService;
		this.dictTypeService = dictTypeService;
		this.dictDataService = dictDataService;
		this.loginLogService = loginLogService;
		this.operLogService = operLogService;
		this.onlineUserService = onlineUserService;
		this.monitorServerService = monitorServerService;
		this.noticeService = noticeService;
		this.jobService = jobService;
		this.configService = configService;
	}

	// ========== 概览 ==========

	@McpTool(name = "dashboard_stats", description = "首页统计：用户数、角色数、菜单数、当前在线数")
	public DashboardStatsVO dashboardStats() {
		return dashboardService.getStats();
	}

	@McpTool(name = "get_server_overview", description = "服务器监控概览：JVM 内存、CPU、磁盘、系统信息")
	public Object getServerOverview() {
		return monitorServerService.getOverview();
	}

	// ========== 用户 / 角色 / 部门 ==========

	@McpTool(name = "query_users", description = "分页查询用户列表，返回用户信息、所属部门与角色")
	public PageResult<?> queryUsers(
			@McpToolParam(description = "用户名（模糊匹配）", required = false) String username,
			@McpToolParam(description = "手机号（模糊匹配）", required = false) String phone,
			@McpToolParam(description = "部门ID（精确匹配）", required = false) Long deptId,
			@McpToolParam(description = "状态：1=正常，0=停用", required = false) Integer status,
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		UserDTO dto = new UserDTO();
		dto.setUsername(username);
		dto.setPhone(phone);
		dto.setDeptId(deptId);
		dto.setStatus(status);
		dto.setPage(normalizePage(page));
		dto.setPageSize(normalizePageSize(pageSize));
		return userService.queryPage(dto);
	}

	@McpTool(name = "get_user", description = "按ID查询用户详情：基本信息、部门、岗位、角色")
	public Object getUser(@McpToolParam(description = "用户ID", required = true) Long id) {
		return userService.findById(id);
	}

	@McpTool(name = "list_roles", description = "查询全部角色（含数据权限范围 dataScope：1=全部 2=本部门 3=本部门及下级 4=仅本人 5=自定义部门）")
	public Object listRoles() {
		return roleService.listAll();
	}

	@McpTool(name = "list_depts", description = "查询部门树（含部门状态与负责人）")
	public Object listDepts() {
		return deptService.getDeptTree();
	}

	// ========== 字典 ==========

	@McpTool(name = "list_dict_types", description = "查询全部启用的字典类型")
	public Object listDictTypes() {
		return dictTypeService.findAllEnabled();
	}

	@McpTool(name = "get_dict_data", description = "按字典类型编码查询字典数据项（如 sys_status、sys_gender）")
	public Object getDictData(@McpToolParam(description = "字典类型编码", required = true) String dictType) {
		return dictDataService.listByDictType(dictType);
	}

	// ========== 日志 / 在线用户 ==========

	@McpTool(name = "query_login_logs", description = "分页查询登录日志")
	public PageResult<?> queryLoginLogs(
			@McpToolParam(description = "用户名（模糊匹配）", required = false) String username,
			@McpToolParam(description = "登录结果：0=成功，1=失败", required = false) Integer status,
			@McpToolParam(description = "登录IP（模糊匹配）", required = false) String ip,
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		return loginLogService.queryPage(username, status, ip, null, null, normalizePage(page),
				normalizePageSize(pageSize));
	}

	@McpTool(name = "query_oper_logs", description = "分页查询操作日志")
	public PageResult<?> queryOperLogs(
			@McpToolParam(description = "操作模块标题（模糊匹配，如 用户管理）", required = false) String title,
			@McpToolParam(description = "操作人（模糊匹配）", required = false) String operUser,
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		return operLogService.queryPage(title, null, operUser, null, null, null, normalizePage(page),
				normalizePageSize(pageSize));
	}

	@McpTool(name = "list_online_users", description = "分页查询当前在线用户")
	public PageResult<?> listOnlineUsers(
			@McpToolParam(description = "用户名（模糊匹配）", required = false) String username,
			@McpToolParam(description = "登录IP（模糊匹配）", required = false) String ip,
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		return onlineUserService.queryPage(username, ip, normalizePage(page), normalizePageSize(pageSize));
	}

	// ========== 企业能力 ==========

	@McpTool(name = "list_published_notices", description = "查询已发布的公告列表（按发布时间倒序）")
	public List<?> listPublishedNotices(
			@McpToolParam(description = "公告类型ID（不过滤传 null）", required = false) Integer type,
			@McpToolParam(description = "返回条数上限，默认 10，最大 50", required = false) Integer limit) {
		int bounded = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
		return noticeService.findPublished(type, bounded);
	}

	@McpTool(name = "list_jobs", description = "分页查询定时任务（含 cron 表达式与运行状态）")
	public PageResult<?> listJobs(
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		JobDTO dto = new JobDTO();
		dto.setPage(normalizePage(page));
		dto.setPageSize(normalizePageSize(pageSize));
		return jobService.queryPage(dto);
	}

	@McpTool(name = "list_configs", description = "分页查询系统参数配置")
	public PageResult<?> listConfigs(
			@McpToolParam(description = "页码，默认 1", required = false) Integer page,
			@McpToolParam(description = "每页条数，默认 10，最大 100", required = false) Integer pageSize) {
		ConfigDTO dto = new ConfigDTO();
		dto.setPage(normalizePage(page));
		dto.setPageSize(normalizePageSize(pageSize));
		return configService.queryPage(dto);
	}

	// ========== 分页参数收敛 ==========

	private int normalizePage(Integer page) {
		return page == null || page < 1 ? 1 : page;
	}

	private int normalizePageSize(Integer pageSize) {
		if (pageSize == null || pageSize < 1) {
			return 10;
		}
		return Math.min(pageSize, 100);
	}

}
