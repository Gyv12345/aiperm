package com.devlovecode.aiperm.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
@Schema(description = "操作日志")
public class SysOperLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "操作模块")
    private String title;

    @Schema(description = "操作类型：1-新增 2-修改 3-删除 4-查询")
    private Integer operType;

    @Schema(description = "方法名")
    private String method;

    @Schema(description = "HTTP请求方式")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回参数")
    private String jsonResult;

    @Schema(description = "操作状态：0-成功 1-失败")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "消耗时间（ms）")
    private Long costTime;

    @Schema(description = "操作人账号")
    private String operUser;

    @Schema(description = "操作人名称")
    private String operName;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
