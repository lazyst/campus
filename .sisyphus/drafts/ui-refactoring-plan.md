# 前端重构计划：移除Vant-UI，使用自定义UI

## 当前使用vant-ui的组件

共27个文件使用vant-ui组件，需要全部重构。

## 重构任务列表

### Phase 1: 创建自定义UI组件库
- BaseButton - 按钮组件
- BaseInput - 输入框组件  
- BaseIcon - 图标组件
- BaseCard - 卡片组件
- BaseCell - 单元格组件
- NavBar - 顶部导航栏
- TabBar - 底部导航栏
- Tabs - 标签页
- List - 列表容器
- Empty - 空状态
- Form/FormItem - 表单组件
- Popup - 弹窗
- FloatingButton - 浮动按钮

### Phase 2: 核心页面重构
- MainLayout.vue - 主布局
- home/Forum.vue - 论坛首页
- home/Trade.vue - 闲置市场
- home/Messages.vue - 消息页面
- home/Profile.vue - 个人中心

### Phase 3: 认证页面
- login/index.vue - 登录页
- register/index.vue - 注册页

### Phase 4: 功能页面重构
- forum模块 (4个文件)
- trade模块 (4个文件)  
- messages模块 (2个文件)
- profile模块 (7个文件)

### Phase 5: 样式系统
- CSS变量定义
- 基础样式重置
- 工具类

## 预计工作量: 10-12小时

需要执行 `/start-work` 开始实施重构计划。
