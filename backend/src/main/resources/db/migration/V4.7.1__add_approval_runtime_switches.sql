-- V4.7.1 审批运行时开关

INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `remark`)
VALUES
('approval.module.enabled', '0', 'system', '审批模块总开关:0关闭,1开启。关闭时基础系统不受影响，审批提交按 optional/required 策略处理');
