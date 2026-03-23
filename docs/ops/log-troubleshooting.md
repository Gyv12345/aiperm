# AiPerm 单机日志排障手册

适用场景：2C4G 单机部署、`SPRING_PROFILES_ACTIVE=prod`、日志写入 `/var/log/aiperm/aiperm.log`。

## 1. 快速查看服务状态

```bash
systemctl status aiperm --no-pager
journalctl -u aiperm -n 100 --no-pager
```

## 2. 用 less 查看实时日志

```bash
less +G /var/log/aiperm/aiperm.log
```

- 在 `less` 内按 `Shift+F` 进入追踪模式（类似 `tail -f`）
- 按 `Ctrl+C` 退出追踪模式
- 输入 `/ERROR` 或 `/Exception` 快速检索
- 输入 `n` 跳到下一个命中

## 3. 常用过滤命令

```bash
# 最近 200 行中的错误
tail -n 200 /var/log/aiperm/aiperm.log | rg "ERROR|Exception|FAILED"

# 按 traceId 关联一次请求链路
rg "trace=<traceId>" /var/log/aiperm/aiperm.log

# 查某个接口
rg "GET /api|POST /api" /var/log/aiperm/aiperm.log
```

## 4. Actuator 快速自检

```bash
curl -s http://127.0.0.1:8080/actuator/health
curl -s http://127.0.0.1:8080/actuator/metrics
```

## 5. 常见问题定位顺序

1. 先看进程是否存活：`systemctl status aiperm`
2. 再看启动日志：`journalctl -u aiperm -b --no-pager`
3. 再看应用文件日志：`less +G /var/log/aiperm/aiperm.log`
4. 用 `traceId` 把同一次请求的日志串起来
5. 必要时再查 `health/metrics` 验证依赖（DB/Redis）状态

## 6. 生产环境建议

1. 日志目录权限：`/var/log/aiperm` 归属 `aiperm:aiperm`
2. 环境变量放入 `/etc/aiperm/aiperm.env`，不要写入仓库
3. 出现磁盘压力时，优先收紧日志级别和保留策略
