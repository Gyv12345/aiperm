package com.devlovecode.aiperm.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "首页统计数据")
public class DashboardStatsVO {

	@Schema(description = "用户总数")
	private Long userCount;

	@Schema(description = "角色数量")
	private Long roleCount;

	@Schema(description = "菜单/权限数量")
	private Long menuCount;

	@Schema(description = "在线用户数")
	private Long onlineCount;

}
