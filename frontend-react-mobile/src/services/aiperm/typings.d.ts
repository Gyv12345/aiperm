declare namespace API {
  type AdminDiagnostics = {
    platformChecks?: PlatformCheck[];
    sceneChecks?: SceneCheck[];
    latestApprovalCallback?: CallbackSummary;
    latestMessagePush?: MessagePushSummary;
  };

  type ApprovalHandlerVO = {
    beanName?: string;
    displayName?: string;
  };

  type ApprovalInstanceDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 场景编码 */
    sceneCode?: string;
    /** 业务类型 */
    businessType?: string;
    /** 平台 */
    platform?: string;
    /** 审批状态 */
    status?: string;
  };

  type ApprovalInstanceVO = {
    id?: number;
    sceneCode?: string;
    sceneName?: string;
    businessType?: string;
    businessId?: number;
    initiatorId?: number;
    initiatorName?: string;
    platform?: string;
    platformInstanceId?: string;
    status?: string;
    errorMessage?: string;
    createTime?: string;
    resultTime?: string;
  };

  type ApprovalSceneDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 场景编码 */
    sceneCode: string;
    /** 场景名称 */
    sceneName: string;
    /** 业务类型 */
    businessType: string;
    /** 平台 */
    platform: string;
    /** 模板ID */
    templateId: string;
    /** 是否启用 */
    enabled?: number;
    /** 处理器Bean名称 */
    handlerBeanName: string;
    /** 是否自动提交审批 */
    autoSubmitEnabled?: number;
    /** 是否允许重复待审 */
    allowDuplicatePending?: number;
    /** 超时时间（小时） */
    timeoutHours?: number;
    /** 超时动作 */
    timeoutAction?: string;
    /** 通知模板编码 */
    notifyTemplateCode?: string;
    /** 备注 */
    remark?: string;
  };

  type ApprovalSceneVO = {
    id?: number;
    sceneCode?: string;
    sceneName?: string;
    businessType?: string;
    platform?: string;
    templateId?: string;
    enabled?: number;
    handlerBeanName?: string;
    autoSubmitEnabled?: number;
    allowDuplicatePending?: number;
    timeoutHours?: number;
    timeoutAction?: string;
    notifyTemplateCode?: string;
    remark?: string;
    createTime?: string;
  };

  type ApprovalSubmitDTO = {
    /** 场景编码 */
    sceneCode: string;
    /** 业务类型 */
    businessType: string;
    /** 业务ID */
    businessId: number;
    /** 业务载荷 */
    payload?: Record<string, any>;
    /** 是否强制要求审批能力已就绪。true: 未启用/未配置IM时报错；false: 直接跳过审批 */
    required?: boolean;
  };

  type ApprovalTodoOverviewVO = {
    viewer?: Viewer;
    userGuide?: UserGuide;
    quickActions?: QuickAction[];
    adminDiagnostics?: AdminDiagnostics;
  };

  type assignMenusParams = {
    id: number;
  };

  type bindCallbackParams = {
    platform: string;
    code: string;
  };

  type bindRedirectParams = {
    platform: string;
  };

  type CacheEntryVO = {
    /** 缓存名称 */
    cacheName?: string;
    /** Redis Key 前缀 */
    keyPrefix?: string;
    /** 估算 Key 数量 */
    estimatedSize?: number;
    /** 样例 TTL（秒） */
    sampleTtl?: number;
  };

  type CacheMonitorVO = {
    /** Redis 已用内存文本 */
    usedMemoryHuman?: string;
    /** 连接客户端数 */
    connectedClients?: number;
    /** 数据库总 Key 数 */
    totalKeys?: number;
    /** 命中次数 */
    hits?: number;
    /** 未命中次数 */
    misses?: number;
    /** 命中率 */
    hitRate?: number;
    /** 缓存条目列表 */
    entries?: CacheEntryVO[];
  };

  type callbackParams = {
    platform: string;
  };

  type CallbackSummary = {
    platform?: string;
    sceneCode?: string;
    platformInstanceId?: string;
    status?: string;
    resultTime?: string;
  };

  type CaptchaConfigDTO = {
    enabled?: number;
    smsProvider?: string;
    smsAccessKey?: string;
    smsSecretKey?: string;
    smsSignName?: string;
    smsTemplateCode?: string;
    emailHost?: string;
    emailPort?: number;
    emailUsername?: string;
    emailPassword?: string;
    emailFrom?: string;
    emailFromName?: string;
    codeLength?: number;
    expireMinutes?: number;
    dailyLimit?: number;
  };

  type CaptchaConfigVO = {
    id?: number;
    type?: string;
    enabled?: number;
    smsProvider?: string;
    smsAccessKey?: string;
    smsSignName?: string;
    smsTemplateCode?: string;
    emailHost?: string;
    emailPort?: number;
    emailUsername?: string;
    emailFrom?: string;
    emailFromName?: string;
    codeLength?: number;
    expireMinutes?: number;
    dailyLimit?: number;
  };

  type CaptchaVO = {
    /** 验证码Key */
    captchaKey?: string;
    /** 验证码图片（Base64） */
    captchaImage?: string;
  };

  type changeStatusParams = {
    id: number;
    status: number;
  };

  type ConfigDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 配置键 */
    configKey: string;
    /** 配置值 */
    configValue?: string;
    /** 配置类型 */
    configType?: string;
    /** 备注 */
    remark?: string;
  };

  type ConfigVO = {
    /** 配置ID */
    id?: number;
    /** 配置键 */
    configKey?: string;
    /** 配置值 */
    configValue?: string;
    /** 配置类型 */
    configType?: string;
    /** 备注 */
    remark?: string;
    /** 创建时间 */
    createTime?: string;
  };

  type DashboardStatsVO = {
    /** 用户总数 */
    userCount?: number;
    /** 角色数量 */
    roleCount?: number;
    /** 菜单/权限数量 */
    menuCount?: number;
    /** 在线用户数 */
    onlineCount?: number;
  };

  type delete10Params = {
    id: number;
  };

  type delete11Params = {
    id: number;
  };

  type delete12Params = {
    id: number;
  };

  type delete13Params = {
    id: number;
  };

  type delete14Params = {
    fileName: string;
  };

  type delete15Params = {
    id: number;
  };

  type delete16Params = {
    id: number;
  };

  type delete1Params = {
    id: number;
  };

  type delete2Params = {
    id: number;
  };

  type delete3Params = {
    id: number;
  };

  type delete4Params = {
    id: number;
  };

  type delete5Params = {
    id: number;
  };

  type delete6Params = {
    id: number;
  };

  type delete7Params = {
    id: number;
  };

  type delete8Params = {
    id: number;
  };

  type delete9Params = {
    id: number;
  };

  type deleteUsingDELETEParams = {
    id: number;
  };

  type DeptDTO = {
    /** 部门名称 */
    deptName: string;
    /** 父部门ID（0为根部门） */
    parentId?: number;
    /** 显示顺序 */
    sort?: number;
    /** 负责人 */
    leader?: string;
    /** 联系电话 */
    phone?: string;
    /** 邮箱 */
    email?: string;
    /** 部门状态（0=正常，1=停用） */
    status?: number;
    /** 备注 */
    remark?: string;
  };

  type detail1Params = {
    id: number;
  };

  type detail2Params = {
    id: number;
  };

  type detail3Params = {
    id: number;
  };

  type detail4Params = {
    id: number;
  };

  type detail5Params = {
    id: number;
  };

  type detail6Params = {
    id: number;
  };

  type detailParams = {
    platform: string;
  };

  type DictDataDTO = {
    /** 字典类型 */
    dictType?: string;
    /** 字典标签（显示值） */
    dictLabel: string;
    /** 字典键值（存储值） */
    dictValue: string;
    /** 排序 */
    sort?: number;
    /** 状态：0-禁用 1-启用 */
    status?: number;
    /** 样式属性（tag类型或十六进制颜色，如 success、#ff5500） */
    listClass?: string;
    /** 备注 */
    remark?: string;
  };

  type DictDataVO = {
    /** 字典数据ID */
    id?: number;
    /** 字典类型 */
    dictType?: string;
    /** 字典标签（显示值） */
    dictLabel?: string;
    /** 字典键值（存储值） */
    dictValue?: string;
    /** 排序 */
    sort?: number;
    /** 状态：0-禁用 1-启用 */
    status?: number;
    /** 样式属性（tag类型或十六进制颜色） */
    listClass?: string;
    /** 备注 */
    remark?: string;
  };

  type DictTypeDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 字典名称 */
    dictName: string;
    /** 字典类型 */
    dictType?: string;
    /** 状态：0-禁用 1-启用 */
    status?: number;
    /** 备注 */
    remark?: string;
  };

  type DictTypeVO = {
    /** 字典类型ID */
    id?: number;
    /** 字典名称 */
    dictName?: string;
    /** 字典类型标识 */
    dictType?: string;
    /** 状态：0-禁用 1-启用 */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 创建时间 */
    createTime?: string;
  };

  type export1Params = {
    dto: DictTypeDTO;
  };

  type export2Params = {
    dictType?: string;
  };

  type export3Params = {
    username?: string;
    ip?: string;
  };

  type export4Params = {
    username?: string;
    status?: number;
    ip?: string;
    startDate?: string;
    endDate?: string;
  };

  type export5Params = {
    jobName?: string;
    status?: number;
    triggerSource?: string;
    startDate?: string;
    endDate?: string;
  };

  type export6Params = {
    title?: string;
    status?: number;
    operUser?: string;
    operIp?: string;
    startDate?: string;
    endDate?: string;
  };

  type exportUsingGETParams = {
    dto: UserDTO;
  };

  type feedParams = {
    type?: number;
    limit?: number;
  };

  type forceLogoutParams = {
    id: number;
  };

  type getById1Params = {
    id: number;
  };

  type getById2Params = {
    id: number;
  };

  type getById3Params = {
    id: number;
  };

  type getById4Params = {
    id: number;
  };

  type getById5Params = {
    id: number;
  };

  type getByIdParams = {
    id: number;
  };

  type getByKeyParams = {
    configKey: string;
  };

  type getChildren1Params = {
    parentId: number;
  };

  type getChildrenParams = {
    parentId: number;
  };

  type getConfig1Params = {
    type: string;
  };

  type getConfigParams = {
    platform: string;
  };

  type getLoginLogsParams = {
    pageNum?: number;
    pageSize?: number;
  };

  type getRoleMenusParams = {
    id: number;
  };

  type HealthComponentVO = {
    /** 组件名称 */
    name?: string;
    /** 状态 */
    status?: string;
    /** 详情 */
    details?: string;
  };

  type ImConfigDTO = {
    /** 是否启用 */
    enabled: number;
    /** 应用ID */
    appId?: string;
    /** 应用密钥 */
    appSecret?: string;
    /** 企业ID */
    corpId?: string;
    /** 回调验证Token */
    callbackToken?: string;
    /** 回调AES Key */
    callbackAesKey?: string;
    /** 扩展配置JSON */
    extraConfig?: string;
    /** 备注 */
    remark?: string;
  };

  type ImConfigVO = {
    id?: number;
    platform?: string;
    enabled?: number;
    appId?: string;
    appSecret?: string;
    corpId?: string;
    callbackToken?: string;
    callbackAesKey?: string;
    extraConfig?: string;
    remark?: string;
    configReady?: boolean;
    missingFields?: string[];
  };

  type ImportErrorVO = {
    /** Excel 行号 */
    rowNumber?: number;
    /** 错误信息 */
    message?: string;
  };

  type ImportResultVO = {
    /** 成功条数 */
    successCount?: number;
    /** 失败条数 */
    failureCount?: number;
    /** 错误列表 */
    errors?: ImportErrorVO[];
  };

  type JobDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 任务名称 */
    jobName: string;
    /** 任务分组 */
    jobGroup?: string;
    /** Cron表达式 */
    cronExpression: string;
    /** 执行类 */
    beanClass: string;
    /** 状态：0-暂停 1-运行 */
    status?: number;
    /** 错过策略（兼容字段） */
    misfirePolicy?: number;
    /** 并发策略（兼容字段） */
    concurrent?: number;
    /** 备注 */
    remark?: string;
    /** 执行目标（前端兼容字段） */
    invokeTarget?: string;
  };

  type JobLogVO = {
    /** 日志ID */
    id?: number;
    /** 任务ID */
    jobId?: number;
    /** 任务名称 */
    jobName?: string;
    /** 任务分组 */
    jobGroup?: string;
    /** 执行目标 */
    beanClass?: string;
    /** 触发来源 */
    triggerSource?: string;
    /** 执行状态 */
    status?: number;
    /** 执行结果 */
    message?: string;
    /** 异常信息 */
    exceptionInfo?: string;
    /** 开始时间 */
    startTime?: string;
    /** 结束时间 */
    endTime?: string;
    /** 耗时(ms) */
    costTime?: number;
  };

  type JobVO = {
    /** 任务ID */
    id?: number;
    /** 任务名称 */
    jobName?: string;
    /** 任务分组 */
    jobGroup?: string;
    /** Cron表达式 */
    cronExpression?: string;
    /** 执行类 */
    beanClass?: string;
    /** 状态：0-暂停 1-运行 */
    status?: number;
    /** 错过策略（兼容字段） */
    misfirePolicy?: number;
    /** 并发策略（兼容字段） */
    concurrent?: number;
    /** 备注 */
    remark?: string;
    /** 创建时间 */
    createTime?: string;
    /** 执行目标（前端兼容字段） */
    invokeTarget?: string;
  };

  type list2Params = {
    dto: DictTypeDTO;
  };

  type list4Params = {
    dto: NoticeDTO;
  };

  type list5Params = {
    dto: MessageDTO;
  };

  type list6Params = {
    dto: JobDTO;
  };

  type list7Params = {
    dto: ConfigDTO;
  };

  type listByDictTypeParams = {
    dto: DictDataDTO;
  };

  type login1Params = {
    platform: string;
  };

  type loginCallbackParams = {
    platform: string;
    code: string;
  };

  type LoginConfigVO = {
    /** 密码登录是否启用 */
    passwordEnabled?: boolean;
    /** 短信验证码登录是否启用 */
    smsEnabled?: boolean;
    /** 邮箱验证码登录是否启用 */
    emailEnabled?: boolean;
    /** OAuth 登录配置列表 */
    oauthConfigs?: OAuthConfig[];
  };

  type LoginLogVO = {
    /** 日志ID */
    id?: number;
    /** 登录IP */
    ip?: string;
    /** 登录地点 */
    location?: string;
    /** 浏览器 */
    browser?: string;
    /** 操作系统 */
    os?: string;
    /** 登录状态（0=成功，1=失败） */
    status?: number;
    /** 提示消息 */
    msg?: string;
    /** 登录时间 */
    loginTime?: string;
  };

  type LoginRequest = {
    /** 用户名 */
    username: string;
    /** 密码 */
    password: string;
    /** 验证码 */
    captcha?: string;
    /** 验证码Key */
    captchaKey?: string;
  };

  type LoginVO = {
    /** 访问令牌 */
    token?: string;
    /** 用户信息 */
    userInfo?: UserInfo;
  };

  type markAsReadParams = {
    id: number;
  };

  type MenuDTO = {
    /** 菜单名称 */
    menuName: string;
    /** 父菜单ID（0为根菜单） */
    parentId?: number;
    /** 菜单类型（M=目录，C=菜单，F=按钮） */
    menuType: string;
    /** 显示顺序 */
    sort?: number;
    /** 路由地址 */
    path?: string;
    /** 组件路径 */
    component?: string;
    /** 权限标识（按钮用，如 system:user:add） */
    perms?: string;
    /** 菜单图标 */
    icon?: string;
    /** 是否为外链（0=否，1=是） */
    isExternal?: number;
    /** 是否缓存（0=不缓存，1=缓存） */
    isCache?: number;
    /** 是否显示（0=隐藏，1=显示） */
    visible?: number;
    /** 菜单状态（0=正常，1=停用） */
    status?: number;
    /** 权限标识 */
    permission?: string;
    /** 备注 */
    remark?: string;
  };

  type MenuVO = {
    /** 菜单ID */
    id?: number;
    /** 父菜单ID */
    parentId?: number;
    /** 菜单名称 */
    menuName?: string;
    /** 菜单类型（M=目录，C=菜单，F=按钮） */
    menuType?: string;
    /** 路由地址 */
    path?: string;
    /** 组件路径 */
    component?: string;
    /** 权限标识 */
    permission?: string;
    /** 菜单图标 */
    icon?: string;
    /** 排序 */
    sort?: number;
    /** 是否可见（0=隐藏，1=显示） */
    visible?: number;
    /** 是否缓存（0=不缓存，1=缓存） */
    isCache?: number;
    /** 是否外链（0=否，1=是） */
    isFrame?: number;
    /** 状态（0=禁用，1=启用） */
    status?: number;
    /** 子菜单列表 */
    children?: MenuVO[];
    /** 创建时间 */
    createTime?: string;
    /** 更新时间 */
    updateTime?: string;
  };

  type MessageDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 接收人ID */
    receiverId?: number;
    /** 标题 */
    title?: string;
    /** 内容 */
    content?: string;
    /** 是否已读：0-未读 1-已读 */
    isRead?: number;
    /** 消息箱体：1-收件箱 2-发件箱 */
    boxType?: number;
    /** 消息ID列表（用于批量已读） */
    ids?: number[];
  };

  type MessagePushSummary = {
    platform?: string;
    templateCode?: string;
    status?: string;
    errorMsg?: string;
    sendTime?: string;
  };

  type MessageReceiverVO = {
    /** 用户ID */
    id?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 真实姓名 */
    realName?: string;
    /** 展示名称 */
    displayName?: string;
  };

  type MessageVO = {
    /** ID */
    id?: number;
    /** 发送人ID */
    senderId?: number;
    /** 发送人名称 */
    senderName?: string;
    /** 接收人ID */
    receiverId?: number;
    /** 接收人名称 */
    receiverName?: string;
    /** 标题 */
    title?: string;
    /** 内容 */
    content?: string;
    /** 是否已读：0-未读 1-已读 */
    isRead?: number;
    /** 阅读时间 */
    readTime?: string;
    /** 创建时间 */
    createTime?: string;
  };

  type MfaPolicyDTO = {
    page?: number;
    pageSize?: number;
    name: string;
    permPattern?: string;
    apiPattern?: string;
    enabled?: number;
  };

  type MfaPolicyVO = {
    id?: number;
    name?: string;
    permPattern?: string;
    apiPattern?: string;
    enabled?: number;
    createTime?: string;
  };

  type MfaQrcodeVO = {
    /** TOTP URI（用于生成二维码） */
    totpUri?: string;
    /** 密钥（用于手动输入） */
    secretKey?: string;
  };

  type MfaStatusVO = {
    /** 是否已绑定 */
    bound?: boolean;
    /** 是否强制要求（超管必须绑定） */
    required?: boolean;
    /** 当前Redis中是否已验证（30分钟内） */
    verified?: boolean;
  };

  type MfaVerifyDTO = {
    /** TOTP 6位验证码 */
    code: string;
  };

  type NoticeDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 标题 */
    title: string;
    /** 内容 */
    content?: string;
    /** 类型：1-通知 2-公告 */
    type?: number;
    /** 状态：0-草稿 1-发布 */
    status?: number;
  };

  type NoticeVO = {
    /** ID */
    id?: number;
    /** 标题 */
    title?: string;
    /** 内容 */
    content?: string;
    /** 类型：1-通知 2-公告 */
    type?: number;
    /** 状态：0-草稿 1-发布 */
    status?: number;
    /** 发布时间 */
    publishTime?: string;
    /** 创建时间 */
    createTime?: string;
    /** 创建人 */
    createBy?: string;
  };

  type OauthBindingVO = {
    /** 平台：WEWORK/DINGTALK/FEISHU */
    platform?: string;
    /** 第三方昵称 */
    nickname?: string;
    /** 第三方头像 */
    avatar?: string;
    /** 绑定时间 */
    createTime?: string;
    /** 最后登录时间 */
    lastLoginTime?: string;
  };

  type OAuthConfig = {
    platform?: string;
    displayName?: string;
    icon?: string;
    enabled?: boolean;
  };

  type OauthConfigDTO = {
    enabled?: number;
    corpId?: string;
    agentId?: string;
    appKey?: string;
    appSecret?: string;
    callbackUrl?: string;
    remark?: string;
  };

  type OauthConfigVO = {
    id?: number;
    platform?: string;
    enabled?: number;
    corpId?: string;
    agentId?: string;
    appKey?: string;
    callbackUrl?: string;
    remark?: string;
  };

  type OnlineUserVO = {
    /** 记录ID */
    id?: number;
    /** 用户ID */
    userId?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 部门名称 */
    deptName?: string;
    /** 角色名称 */
    roleNames?: string;
    /** Token */
    token?: string;
    /** 登录IP */
    ip?: string;
    /** 浏览器 */
    browser?: string;
    /** 操作系统 */
    os?: string;
    /** 登录时间 */
    loginTime?: string;
    /** 最后活跃时间 */
    lastAccessTime?: string;
    /** Token 剩余秒数 */
    tokenTimeout?: number;
    /** 是否当前会话 */
    currentSession?: boolean;
  };

  type OssResult = {
    fileName?: string;
    originalName?: string;
    url?: string;
    size?: number;
    contentType?: string;
  };

  type overview2Params = {
    platform?: string;
  };

  type page1Params = {
    dto: RoleDTO;
  };

  type page2Params = {
    dto: PostDTO;
  };

  type page3Params = {
    dto: ApprovalSceneDTO;
  };

  type page4Params = {
    dto: ApprovalInstanceDTO;
  };

  type page5Params = {
    page?: number;
    pageSize?: number;
    username?: string;
    ip?: string;
  };

  type page6Params = {
    page?: number;
    pageSize?: number;
    username?: string;
    status?: number;
    ip?: string;
    startDate?: string;
    endDate?: string;
  };

  type page7Params = {
    page?: number;
    pageSize?: number;
    jobName?: string;
    status?: number;
    triggerSource?: string;
    startDate?: string;
    endDate?: string;
  };

  type page8Params = {
    page?: number;
    pageSize?: number;
    title?: string;
    status?: number;
    operUser?: string;
    operIp?: string;
    startDate?: string;
    endDate?: string;
  };

  type pageParams = {
    dto: UserDTO;
  };

  type PageResultApprovalInstanceVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: ApprovalInstanceVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultApprovalSceneVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: ApprovalSceneVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultConfigVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: ConfigVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultDictTypeVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: DictTypeVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultJobLogVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: JobLogVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultJobVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: JobVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultLoginLogVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: LoginLogVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultMessageVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: MessageVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultNoticeVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: NoticeVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultOnlineUserVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: OnlineUserVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultSysOperLog = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: SysOperLog[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultSysPost = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: SysPost[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultSysRole = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: SysRole[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PageResultUserVO = {
    /** 总记录数 */
    total?: number;
    /** 数据列表 */
    list?: UserVO[];
    /** 当前页码 */
    pageNum?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 总页数 */
    pages?: number;
  };

  type PasswordDTO = {
    /** 旧密码 */
    oldPassword: string;
    /** 新密码 */
    newPassword: string;
  };

  type pauseParams = {
    id: number;
  };

  type PlatformCheck = {
    platform?: string;
    enabled?: boolean;
    configReady?: boolean;
    missingFields?: string[];
  };

  type PostDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 岗位名称 */
    postName: string;
    /** 岗位编码 */
    postCode: string;
    /** 显示顺序 */
    sort?: number;
    /** 岗位状态（0=正常，1=停用） */
    status?: number;
    /** 备注 */
    remark?: string;
  };

  type ProfileDTO = {
    /** 昵称 */
    nickname?: string;
    /** 真实姓名 */
    realName?: string;
    /** 用户邮箱 */
    email?: string;
    /** 手机号码 */
    phone?: string;
    /** 用户性别（0=未知，1=男，2=女） */
    gender?: number;
    /** 头像地址 */
    avatar?: string;
  };

  type ProfileVO = {
    /** 用户ID */
    id?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 真实姓名 */
    realName?: string;
    /** 邮箱 */
    email?: string;
    /** 手机号 */
    phone?: string;
    /** 性别（0=未知，1=男，2=女） */
    gender?: number;
    /** 头像 */
    avatar?: string;
    /** 部门ID */
    deptId?: number;
    /** 部门名称 */
    deptName?: string;
    /** 岗位名称 */
    postName?: string;
    /** 角色名称列表 */
    roleNames?: string[];
    /** 状态（0=正常，1=停用） */
    status?: number;
    /** 最后登录IP */
    lastLoginIp?: string;
    /** 最后登录时间 */
    lastLoginTime?: string;
    /** 创建时间 */
    createTime?: string;
  };

  type publishedParams = {
    type?: number;
    limit?: number;
  };

  type publishParams = {
    id: number;
  };

  type QuickAction = {
    code?: string;
    enabled?: boolean;
    reason?: string;
    url?: string;
  };

  type RApprovalSceneVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ApprovalSceneVO;
    success?: boolean;
  };

  type RApprovalTodoOverviewVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ApprovalTodoOverviewVO;
    success?: boolean;
  };

  type RCacheMonitorVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: CacheMonitorVO;
    success?: boolean;
  };

  type RCaptchaConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: CaptchaConfigVO;
    success?: boolean;
  };

  type RCaptchaVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: CaptchaVO;
    success?: boolean;
  };

  type RConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ConfigVO;
    success?: boolean;
  };

  type RDashboardStatsVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: DashboardStatsVO;
    success?: boolean;
  };

  type RDictTypeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: DictTypeVO;
    success?: boolean;
  };

  type resetPasswordParams = {
    id: number;
  };

  type resumeParams = {
    id: number;
  };

  type RImConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ImConfigVO;
    success?: boolean;
  };

  type RImportResultVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ImportResultVO;
    success?: boolean;
  };

  type RInteger = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: number;
    success?: boolean;
  };

  type RJobVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: JobVO;
    success?: boolean;
  };

  type RListApprovalHandlerVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ApprovalHandlerVO[];
    success?: boolean;
  };

  type RListDictDataVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: DictDataVO[];
    success?: boolean;
  };

  type RListDictTypeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: DictTypeVO[];
    success?: boolean;
  };

  type RListImConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ImConfigVO[];
    success?: boolean;
  };

  type RListLong = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: number[];
    success?: boolean;
  };

  type RListMenuVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MenuVO[];
    success?: boolean;
  };

  type RListMessageReceiverVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MessageReceiverVO[];
    success?: boolean;
  };

  type RListMfaPolicyVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MfaPolicyVO[];
    success?: boolean;
  };

  type RListNoticeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: NoticeVO[];
    success?: boolean;
  };

  type RListOauthBindingVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: OauthBindingVO[];
    success?: boolean;
  };

  type RListSysDept = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysDept[];
    success?: boolean;
  };

  type RListSysMenu = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysMenu[];
    success?: boolean;
  };

  type RListSysPost = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysPost[];
    success?: boolean;
  };

  type RListSysRole = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysRole[];
    success?: boolean;
  };

  type RLoginConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: LoginConfigVO;
    success?: boolean;
  };

  type RLoginVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: LoginVO;
    success?: boolean;
  };

  type RLong = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: number;
    success?: boolean;
  };

  type RMapStringInteger = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: Record<string, any>;
    success?: boolean;
  };

  type RMessageVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MessageVO;
    success?: boolean;
  };

  type RMfaQrcodeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MfaQrcodeVO;
    success?: boolean;
  };

  type RMfaStatusVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: MfaStatusVO;
    success?: boolean;
  };

  type RNoticeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: NoticeVO;
    success?: boolean;
  };

  type ROauthConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: OauthConfigVO;
    success?: boolean;
  };

  type RoleDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 角色名称 */
    roleName: string;
    /** 角色编码 */
    roleCode: string;
    /** 显示顺序 */
    sort?: number;
    /** 角色状态（0=正常，1=停用） */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 数据权限范围：1-全部，2-本部门，3-本部门及下级，4-仅本人 */
    dataScope?: number;
    /** 角色ID（用于分配菜单） */
    roleId?: number;
    /** 菜单ID列表 */
    menuIds?: number[];
  };

  type RoleVO = {
    /** 角色ID */
    id?: number;
    /** 角色名称 */
    roleName?: string;
    /** 角色编码 */
    roleCode?: string;
    /** 排序 */
    sort?: number;
    /** 状态（0=禁用，1=启用） */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 数据权限范围：1-全部，2-本部门，3-本部门及下级，4-仅本人 */
    dataScope?: number;
    /** 菜单列表 */
    menus?: MenuVO[];
    /** 创建时间 */
    createTime?: string;
    /** 更新时间 */
    updateTime?: string;
  };

  type ROssResult = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: OssResult;
    success?: boolean;
  };

  type RPageResultApprovalInstanceVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultApprovalInstanceVO;
    success?: boolean;
  };

  type RPageResultApprovalSceneVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultApprovalSceneVO;
    success?: boolean;
  };

  type RPageResultConfigVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultConfigVO;
    success?: boolean;
  };

  type RPageResultDictTypeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultDictTypeVO;
    success?: boolean;
  };

  type RPageResultJobLogVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultJobLogVO;
    success?: boolean;
  };

  type RPageResultJobVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultJobVO;
    success?: boolean;
  };

  type RPageResultLoginLogVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultLoginLogVO;
    success?: boolean;
  };

  type RPageResultMessageVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultMessageVO;
    success?: boolean;
  };

  type RPageResultNoticeVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultNoticeVO;
    success?: boolean;
  };

  type RPageResultOnlineUserVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultOnlineUserVO;
    success?: boolean;
  };

  type RPageResultSysOperLog = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultSysOperLog;
    success?: boolean;
  };

  type RPageResultSysPost = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultSysPost;
    success?: boolean;
  };

  type RPageResultSysRole = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultSysRole;
    success?: boolean;
  };

  type RPageResultUserVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: PageResultUserVO;
    success?: boolean;
  };

  type RProfileVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ProfileVO;
    success?: boolean;
  };

  type RServerMonitorVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: ServerMonitorVO;
    success?: boolean;
  };

  type RSysDept = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysDept;
    success?: boolean;
  };

  type RSysMenu = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysMenu;
    success?: boolean;
  };

  type RSysOperLog = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysOperLog;
    success?: boolean;
  };

  type RSysPost = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysPost;
    success?: boolean;
  };

  type RSysRole = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: SysRole;
    success?: boolean;
  };

  type runOnceParams = {
    id: number;
  };

  type RUserInfoVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: UserInfoVO;
    success?: boolean;
  };

  type RUserVO = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: UserVO;
    success?: boolean;
  };

  type RVoid = {
    /** 状态码 */
    code?: number;
    /** 返回消息 */
    message?: string;
    /** 返回数据 */
    data?: any;
    success?: boolean;
  };

  type SceneCheck = {
    platform?: string;
    enabledSceneCount?: number;
    sampleSceneCode?: string;
  };

  type SendCaptchaDTO = {
    /** 目标地址（手机号或邮箱） */
    target: string;
    /** 类型：SMS/EMAIL */
    type: string;
    /** 场景：LOGIN/BIND/RESET */
    scene: string;
  };

  type ServerMonitorVO = {
    /** 应用名称 */
    appName?: string;
    /** 激活环境 */
    activeProfiles?: string[];
    /** 健康状态 */
    status?: string;
    /** 运行时长(毫秒) */
    uptime?: number;
    /** Java 版本 */
    javaVersion?: string;
    /** 操作系统 */
    osName?: string;
    /** CPU 核数 */
    processors?: number;
    /** 系统 CPU 使用率 */
    systemCpuUsage?: number;
    /** 进程 CPU 使用率 */
    processCpuUsage?: number;
    /** 堆内存已用 */
    heapUsed?: number;
    /** 堆内存最大 */
    heapMax?: number;
    /** 非堆内存已用 */
    nonHeapUsed?: number;
    /** 非堆内存最大 */
    nonHeapMax?: number;
    /** 磁盘总量 */
    diskTotal?: number;
    /** 磁盘可用 */
    diskUsable?: number;
    /** 活动线程数 */
    liveThreads?: number;
    /** 守护线程数 */
    daemonThreads?: number;
    /** 峰值线程数 */
    peakThreads?: number;
    /** 健康检查组件 */
    healthComponents?: HealthComponentVO[];
  };

  type SysDept = {
    id?: number;
    createTime?: string;
    updateTime?: string;
    createBy?: string;
    updateBy?: string;
    deleted?: number;
    version?: number;
    deptName?: string;
    parentId?: number;
    sort?: number;
    leader?: string;
    phone?: string;
    email?: string;
    status?: number;
    remark?: string;
    children?: SysDept[];
  };

  type SysMenu = {
    id?: number;
    createTime?: string;
    updateTime?: string;
    createBy?: string;
    updateBy?: string;
    deleted?: number;
    version?: number;
    menuName?: string;
    parentId?: number;
    menuType?: string;
    sort?: number;
    path?: string;
    component?: string;
    perms?: string;
    icon?: string;
    isExternal?: number;
    isCache?: number;
    visible?: number;
    status?: number;
    remark?: string;
    children?: SysMenu[];
    permission?: string;
  };

  type SysOperLog = {
    id?: number;
    title?: string;
    operType?: number;
    method?: string;
    requestMethod?: string;
    operUrl?: string;
    operIp?: string;
    operParam?: string;
    jsonResult?: string;
    status?: number;
    errorMsg?: string;
    costTime?: number;
    operUser?: string;
    operName?: string;
    createTime?: string;
  };

  type SysPost = {
    id?: number;
    createTime?: string;
    updateTime?: string;
    createBy?: string;
    updateBy?: string;
    deleted?: number;
    version?: number;
    postName?: string;
    postCode?: string;
    sort?: number;
    status?: number;
    remark?: string;
  };

  type SysRole = {
    id?: number;
    createTime?: string;
    updateTime?: string;
    createBy?: string;
    updateBy?: string;
    deleted?: number;
    version?: number;
    roleName?: string;
    roleCode?: string;
    sort?: number;
    status?: number;
    remark?: string;
    dataScope?: number;
  };

  type unbind1Params = {
    platform: string;
  };

  type UnifiedLoginDTO = {
    /** 登录类型：PASSWORD/SMS/EMAIL/OAUTH */
    loginType: string;
    /** 登录标识（用户名/手机号/邮箱） */
    identifier: string;
    /** 凭证（密码/验证码/OAuth code） */
    credential: string;
    /** 图形验证码（密码登录时需要） */
    imageCaptcha?: string;
    /** 图形验证码Key（密码登录时需要） */
    imageCaptchaKey?: string;
  };

  type update10Params = {
    id: number;
  };

  type update11Params = {
    id: number;
  };

  type update12Params = {
    id: number;
  };

  type update1Params = {
    id: number;
  };

  type update2Params = {
    id: number;
  };

  type update3Params = {
    id: number;
  };

  type update4Params = {
    id: number;
  };

  type update5Params = {
    platform: string;
  };

  type update6Params = {
    id: number;
  };

  type update7Params = {
    id: number;
  };

  type update8Params = {
    id: number;
  };

  type update9Params = {
    id: number;
  };

  type updateConfig1Params = {
    type: string;
  };

  type updateConfigParams = {
    platform: string;
  };

  type updateParams = {
    id: number;
  };

  type UserDTO = {
    /** 页码 */
    page?: number;
    /** 每页条数 */
    pageSize?: number;
    /** 用户名 */
    username?: string;
    /** 密码 */
    password?: string;
    /** 昵称 */
    nickname?: string;
    /** 真实姓名 */
    realName?: string;
    /** 用户邮箱 */
    email?: string;
    /** 手机号码 */
    phone?: string;
    /** 用户性别（0=未知，1=男，2=女） */
    gender?: number;
    /** 头像地址 */
    avatar?: string;
    /** 部门ID */
    deptId?: number;
    /** 岗位ID */
    postId?: number;
    /** 角色ID列表 */
    roleIds?: number[];
    /** 用户状态（0=正常，1=停用） */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 新密码（重置密码用） */
    newPassword?: string;
  };

  type UserGuide = {
    moduleEnabled?: boolean;
    platformEnabled?: boolean;
    platformConfigReady?: boolean;
    oauthBound?: boolean;
    enabledSceneCount?: number;
    nextStep?: string;
    nextStepReason?: string;
  };

  type UserInfo = {
    /** 用户ID */
    id?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 头像 */
    avatar?: string;
    /** 邮箱 */
    email?: string;
    /** 手机号 */
    phone?: string;
  };

  type UserInfoVO = {
    /** 用户ID */
    id?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 头像 */
    avatar?: string;
    /** 角色列表 */
    roles?: string[];
    /** 权限列表 */
    permissions?: string[];
  };

  type UserVO = {
    /** 用户ID */
    id?: number;
    /** 用户名 */
    username?: string;
    /** 昵称 */
    nickname?: string;
    /** 真实姓名 */
    realName?: string;
    /** 邮箱 */
    email?: string;
    /** 手机号 */
    phone?: string;
    /** 性别（0=未知，1=男，2=女） */
    gender?: number;
    /** 头像 */
    avatar?: string;
    /** 部门ID */
    deptId?: number;
    /** 部门名称 */
    deptName?: string;
    /** 岗位ID列表 */
    postIds?: number[];
    /** 岗位名称 */
    postNames?: string;
    /** 角色ID列表 */
    roleIds?: number[];
    /** 角色名称 */
    roleNames?: string;
    /** 角色列表 */
    roles?: RoleVO[];
    /** 状态（0=禁用，1=启用） */
    status?: number;
    /** 备注 */
    remark?: string;
    /** 创建时间 */
    createTime?: string;
    /** 更新时间 */
    updateTime?: string;
  };

  type Viewer = {
    userId?: number;
    isAdmin?: boolean;
    focusPlatform?: string;
  };

  type withdrawParams = {
    id: number;
  };
}
