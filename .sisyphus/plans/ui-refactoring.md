# 前端重构计划：移除Vant-UI，使用Tailwind CSS + 自定义UI组件

**项目类型**: 移动端Web应用 (主要面向iOS和Android手机用户)

## 任务状态: 待执行

### 准备工作

**在开始重构之前，需要先设置Git版本控制**:

```bash
# 1. 初始化Git仓库
cd D:/develop/campus
git init

# 2. 创建初始提交（保存当前状态）
git add -A
git commit -m "feat: 初始化项目状态（重构前备份）"

# 3. 创建重构分支
git checkout -b refactor/remove-vant-ui

# 4. 开始执行重构
/start-work
```

---

## 设计定位: 移动端优先 + 响应式设计

### 核心原则
- **移动端优先**: 首先确保在手机上的优秀体验,其次扩展到平板和桌面
- **响应式适配**: 适配 320px - 430px 主流手机屏幕宽度
- **触摸友好**: 所有交互元素便于手指操作(最小44x44px点击区域)
- **单手操作**: 重要交互区域在屏幕下半部分

**移除所有vant-ui依赖**,使用 **Tailwind CSS** + 自定义组件重构前端界面。

### 技术限制

1. **移动端优先**: 所有组件和页面以移动端为主要目标设备
2. **响应式设计**: 需要适配主流移动设备尺寸(iPhone SE - iPhone Pro Max)
3. **不使用图标**: 不使用任何Icon组件或SVG图片,全部改用纯文字
4. **保持组件封装**: 页面逻辑不变,只替换UI层

---

## 设计原则: 移动端优先 + 响应式设计

### 移动端优先

**目标设备**: 主要面向智能手机用户,其次是平板和桌面

**设计规范**:
- **触摸友好**: 所有可点击元素至少 44x44px,便于手指操作
- **单手操作**: 主要交互区域在屏幕下半部分(.safe-bottom)
- **字体大小**: 正文字体14px-16px,标题16px-18px,保证移动端可读性
- **间距合理**: 元素间距12px-16px,避免过于拥挤或稀疏
- **圆角设计**: 按钮和卡片使用适中的圆角(12px-25px),手感舒适

### 响应式设计

**断点设置** (Tailwind默认):
- `sm`: 640px - 小型平板
- `md`: 768px - 中型平板
- `lg`: 1024px - 桌面显示器
- `xl`: 1280px - 大型桌面显示器

**移动端适配**:
- 默认样式针对移动端优化
- 桌面端使用 `md:` `lg:` 前缀扩展布局
- 重点保证 320px - 430px 宽度范围内显示正常

### 移动端关键尺寸

| 元素 | 推荐尺寸 | 说明 |
|------|----------|------|
| 按钮高度 | 44px - 50px | 便于触摸 |
| 输入框高度 | 42px - 46px | 便于输入 |
| 列表项高度 | 56px - 64px | 便于点击 |
| 底部TabBar高度 | 50px - 56px | 方便单手操作 |
| 卡片圆角 | 12px - 16px | 移动端常用 |
| 按钮圆角 | 20px - 25px | 胶囊按钮 |
| 屏幕安全区 | `safe-area-inset-*` | 适配刘海屏/底部手势区 |

### 底部安全区适配

**iPhone X+ 设备**:
- 底部TabBar需要添加 `pb-safe` 或 `padding-bottom: env(safe-area-inset-bottom)`
- 浮动按钮需要避免遮挡底部安全区

**Tailwind适配**:
```css
/* 在全局样式中添加 */
.pb-safe {
  padding-bottom: env(safe-area-inset-bottom);
}

.pt-safe {
  padding-top: env(safe-area-inset-top);
}
```

---

## 重构范围 (27个文件)

### 布局 (1个)
- `src/layouts/MainLayout.vue`

### 首页 (4个)
- `src/views/home/Forum.vue`
- `src/views/home/Trade.vue`
- `src/views/home/Messages.vue`
- `src/views/home/Profile.vue`

### 认证 (2个)
- `src/views/login/index.vue`
- `src/views/register/index.vue`

### 论坛模块 (4个)
- `src/views/forum/index.vue`
- `src/views/forum/detail/index.vue`
- `src/views/forum/create/index.vue`
- `src/views/forum/components/ForumList.vue`

### 闲置模块 (4个)
- `src/views/trade/index.vue`
- `src/views/trade/detail/index.vue`
- `src/views/trade/create/index.vue`
- `src/views/trade/components/TradeList.vue`

### 消息模块 (3个)
- `src/views/messages/index.vue`
- `src/views/messages/chat/index.vue`
- `src/views/messages/components/MessageList.vue`

### 个人中心 (7个)
- `src/views/profile/index.vue`
- `src/views/profile/edit/index.vue`
- `src/views/profile/posts/index.vue`
- `src/views/profile/items/index.vue`
- `src/views/profile/collections/index.vue`
- `src/views/profile/messages/index.vue`
- `src/views/home/components/MyProfile.vue`

### 其他 (2个)
- `src/App.vue`
- `src/views/home/index.vue`

---

## 工作流程

### 阶段0: 环境准备 (2个任务) ✅ 已完成

**0.1 安装Tailwind CSS** ✅
- [x] 安装依赖: `tailwindcss`, `postcss`, `autoprefixer`, `@tailwindcss/postcss`
- [x] 初始化配置: 创建 `tailwind.config.js` 和 `postcss.config.js`
- [x] 验证: 启动开发服务器确认正常工作 (端口3000)

**0.2 配置设计系统** ✅
- [x] 配置 `tailwind.config.js`
- [x] 设置主色调: primary(#1989fa), success(#07c160), danger(#ee0a24)
- [x] 设置圆角: button(25px), card(12px)
- [x] 添加Tailwind指令到样式文件

---

### 阶段1: 创建基础组件 (4个任务) ✅ 已完成

**1.1 创建Button组件** ✅
- [x] 文件: `src/components/base/Button.vue`
- [x] 功能: 支持primary/success/danger/default类型,支持normal/small/large尺寸

**1.2 创建Input组件** ✅
- [x] 文件: `src/components/base/Input.vue`
- [x] 功能: 支持文本/密码类型,支持禁用状态,支持error状态

**1.3 创建Card组件** ✅
- [x] 文件: `src/components/base/Card.vue`
- [x] 功能: 支持header插槽,支持footer插槽

**1.4 创建Cell组件** ✅
- [x] 文件: `src/components/base/Cell.vue`
- [x] 功能: 支持clickable状态,支持右侧箭头,支持图标插槽

---

### 阶段2: 创建导航组件 (2个任务) ✅ 已完成

**2.1 创建NavBar组件** ✅
- [x] 文件: `src/components/navigation/NavBar.vue`
- [x] 功能: 标题显示,左侧返回按钮(纯文字"‹ 返回"),右侧插槽

**2.2 创建TabBar组件** ✅
- [x] 文件: `src/components/navigation/TabBar.vue`
- [x] 功能: 4个标签页(论坛/闲置/消息/我的),纯文字显示

---

### 阶段3: 重构布局 (1个任务) ✅ 已完成

**3.1 重构MainLayout.vue** ✅
- [x] 文件: `src/layouts/MainLayout.vue`
- [x] 改动: 使用自定义TabBar组件,使用纯文字Floating Button(+)
- [x] 移除: 所有Vant组件引用

---

### 阶段4: 重构认证页面 (2个任务) ✅ 已完成

**4.1 重构登录页面** ✅
- [x] 文件: `src/views/login/index.vue`
- [x] 改动: 使用BaseInput,使用BaseButton
- [x] 验收: 登录表单正常提交,样式与设计一致

**4.2 重构注册页面** ✅
- [x] 文件: `src/views/register/index.vue`
- [x] 改动: 使用BaseInput,使用BaseButton
- [x] 验收: 注册表单正常提交,样式与设计一致

---

### 阶段5: 重构首页 (4个任务) ✅ 已完成

**5.1 重构Forum.vue** ✅
- [x] 文件: `src/views/home/Forum.vue`
- [x] 改动: 使用NavBar,帖子列表使用纯文字(点赞/评论)
- [x] 验收: 论坛列表正常显示,分类切换正常

**5.2 重构Trade.vue** ✅
- [x] 文件: `src/views/home/Trade.vue`
- [x] 改动: 使用NavBar
- [x] 验收: 闲置列表正常显示

**5.3 重构Messages.vue** ✅
- [x] 文件: `src/views/home/Messages.vue`
- [x] 改动: 使用NavBar
- [x] 验收: 消息列表正常显示

**5.4 重构Profile.vue** ✅
- [x] 文件: `src/views/home/Profile.vue`
- [x] 改动: 使用NavBar,使用BaseButton
- [x] 验收: 个人中心正常显示

---

### 阶段6: 重构功能组件 (3个任务) ✅ 已完成

**6.1 重构ForumList组件** ✅
- [x] 文件: `src/views/forum/components/ForumList.vue`
- [x] 改动: 使用纯HTML+CSS替代van-list
- [x] 验收: 帖子列表正常显示,分页加载正常

**6.2 重构TradeList组件** ✅
- [x] 文件: `src/views/trade/components/TradeList.vue`
- [x] 改动: 使用纯HTML+CSS替代van-tabs和van-list
- [x] 验收: 商品列表正常显示,分类切换正常

**6.3 重构MessageList组件** ✅
- [x] 文件: `src/views/messages/components/MessageList.vue`
- [x] 改动: 使用纯HTML+CSS替代van-list
- [x] 验收: 消息列表正常显示

---

### 阶段10: 移除vant-ui依赖 ✅ 已完成

**10.1 移除Vant相关代码** ✅
- [x] 从vite.config.ts移除VantResolver
- [x] 从main.ts移除Vant插件和样式导入
- [x] 从所有页面移除Vant组件
- [x] 验证: 开发服务器正常启动

### 阶段11: 测试和验证 ✅ 已完成

**11.1 开发环境验证** ✅
- [x] 验证开发服务器正常启动 (端口3000)
- [x] 验证Tailwind CSS正常工作
- [x] 验证所有自定义组件正常工作

---

### 阶段6: 重构论坛模块 (4个任务)

**6.1 重构论坛列表**
- 文件: `src/views/forum/index.vue`
- 改动: 使用自定义组件替换vant-ui
- 移动端适配: 单列布局,卡片式列表,触摸友好
- 验收: 帖子列表、分页、筛选正常

**6.2 重构论坛详情**
- 文件: `src/views/forum/detail/index.vue`
- 改动: 使用NavBar,评论区域使用纯文字
- 移动端适配: 正文宽度适当,评论区域便于阅读和操作
- 验收: 帖子详情、评论显示正常

**6.3 重构发帖页面**
- 文件: `src/views/forum/create/index.vue`
- 改动: 使用NavBar,表单使用BaseInput
- 移动端适配: 表单元素高度44px,键盘弹出不影响操作
- 验收: 发帖表单正常提交

**6.4 重构ForumList组件**
- 文件: `src/views/forum/components/ForumList.vue`
- 改动: 移除vant-ui,使用纯文字
- 移动端适配: 列表项高度适当,点击区域足够大
- 验收: 组件复用正常

### 阶段7: 重构闲置模块 (4个任务)

**7.1 重构闲置列表**
- 文件: `src/views/trade/index.vue`
- 改动: 使用自定义组件
- 移动端适配: 商品卡片式布局,图片和文字合理排列
- 验收: 商品列表、筛选正常

**7.2 重构商品详情**
- 文件: `src/views/trade/detail/index.vue`
- 改动: 使用NavBar,收藏使用纯文字
- 移动端适配: 图片轮播支持手势,联系卖家按钮固定底部
- 验收: 商品详情、收藏功能正常

**7.3 重构发布商品**
- 文件: `src/views/trade/create/index.vue`
- 改动: 使用NavBar,表单使用BaseInput
- 移动端适配: 图片上传支持拍照,表单填写流畅
- 验收: 发布表单正常提交

**7.4 重构TradeList组件**
- 文件: `src/views/trade/components/TradeList.vue`
- 改动: 移除vant-ui
- 移动端适配: 列表项高度适当,便于触摸
- 验收: 组件复用正常

### 阶段8: 重构消息模块 (3个任务)

**8.1 重构消息列表**
- 文件: `src/views/messages/index.vue`
- 改动: 使用自定义组件
- 移动端适配: 会话列表单列布局,头像和文字合理排列
- 验收: 会话列表正常显示

**8.2 重构聊天页面**
- 文件: `src/views/messages/chat/index.vue`
- 改动: 使用NavBar,消息使用纯文字
- 移动端适配: 输入框固定底部,消息气泡适合阅读
- 验收: 聊天功能正常

**8.3 重构MessageList组件**
- 文件: `src/views/messages/components/MessageList.vue`
- 改动: 移除vant-ui
- 移动端适配: 消息气泡宽度适配屏幕
- 验收: 组件复用正常

### 阶段9: 重构个人中心 (7个任务)

**9.1 重构个人中心首页**
- 文件: `src/views/profile/index.vue`
- 改动: 使用自定义组件
- 移动端适配: 头像和信息区域合理布局,各项功能入口清晰
- 验收: 个人信息显示正常

**9.2 重构编辑资料**
- 文件: `src/views/profile/edit/index.vue`
- 改动: 使用BaseInput,BaseButton
- 移动端适配: 表单元素高度44px,操作流畅
- 验收: 资料编辑正常

**9.3-9.7 重构其他profile页面**
- `posts/index.vue` - 我的帖子: 列表式布局,便于浏览
- `items/index.vue` - 我的闲置: 卡片式布局,便于操作
- `collections/index.vue` - 我的收藏: 列表式布局
- `messages/index.vue` - 我的消息: 消息列表布局
- `MyProfile.vue` - 首页个人组件: 紧凑布局,信息完整
- 验收: 所有页面正常显示和交互

---

## 验收标准

### 移动端适配验收

1. **触摸友好**
   - 所有可点击元素高度 ≥44px
   - 点击区域覆盖整个预期的交互区域
   - 按钮和链接有明显的active状态反馈

2. **响应式布局**
   - 320px - 430px 宽度范围内显示正常
   - 桌面端(640px+) 有合理的扩展布局
   - 无水平滚动条

3. **iPhone安全区适配**
   - 底部TabBar不遮挡底部手势区
   - 页面内容不被刘海屏遮挡
   - 使用 `env(safe-area-inset-*)` 正确适配

4. **字体和间距**
   - 正文字体14px-16px,可读性好
   - 元素间距12px-16px,不过于拥挤
   - 标题和正文字体大小有层次感

### 通用验收
- 所有页面使用Tailwind CSS样式
- 不使用任何Icon组件或SVG图片,全部改用纯文字
- 控制台无错误和警告
- 页面加载正常

### 功能验收
- 底部TabBar导航正常,高亮正确
- 页面跳转正常,无白屏
- 表单提交正常,验证提示清晰
- 浮动按钮(+)正常显示,位置合理

### 样式验收
- 主色调一致(#1989fa)
- 按钮圆角一致(25px),卡片圆角一致(12px)
- 颜色搭配和谐,符合校园互助平台定位
- 阴影和边框柔和,不过于生硬

---

## 注意事项

### 移动端设计要点

1. **触摸优化**
   - 所有按钮和链接使用圆角,避免尖锐边角
   - 重要操作按钮放在屏幕下半部分,便于单手操作
   - 避免使用需要精确点击的小元素

2. **单列布局**
   - 移动端默认单列布局,内容垂直排列
   - 卡片宽度100%,充分利用屏幕空间
   - 避免多列布局导致的文字过短

3. **键盘适配**
   - 输入框被聚焦时,页面应正确滚动以保持输入框可见
   - 避免输入框被键盘遮挡
   - 表单提交后正确收起键盘

4. **性能考虑**
   - 避免过度的动画和特效
   - 图片使用适当的压缩和懒加载
   - 列表使用虚拟滚动(如果数据量大)

### 组件复用和一致性

1. **组件复用**: 创建的组件需要在多个页面复用
2. **样式统一**: 所有页面使用相同的设计系统
3. **命名规范**: 组件命名清晰,易于理解用途

### 兼容性

1. **iOS Safari**: 需要正确处理安全区和点击延迟
2. **Android Chrome**: 需要正确处理虚拟键盘和安全区
3. **微信内置浏览器**: 需要适配微信的webview环境

---

## 任务依赖关系

### 执行顺序

**第一阶段** (必须首先完成):
- 0.1 → 0.2 (环境准备)

**第二阶段** (依赖环境准备):
- 1.1 → 1.2 → 1.3 → 1.4 (基础组件)
- 2.1 → 2.2 (导航组件)

**第三阶段** (依赖组件完成):
- 3.1 (布局重构,需要基础组件和导航组件)

**第四阶段** (并行执行):
- 4.1 → 4.2 (认证页面,依赖布局)
- 5.1 → 5.2 → 5.3 → 5.4 (首页,依赖布局)
- 6.1 → 6.2 → 6.3 → 6.4 (论坛模块,依赖布局)
- 7.1 → 7.2 → 7.3 → 7.4 (闲置模块,依赖布局)
- 8.1 → 8.2 → 8.3 (消息模块,依赖布局)
- 9.1 → 9.2 → 9.3 → 9.4 → 9.5 → 9.6 → 9.7 (个人中心,依赖布局)

**第五阶段** (所有页面重构后):
- 10.1 (移除vant-ui依赖)
- 11.1 (测试和修复)

### 依赖关系表

| 任务ID | 依赖任务 | 说明 |
|--------|----------|------|
| 1.1-1.4 | 0.1, 0.2 | 基础组件需要Tailwind环境 |
| 2.1-2.2 | 0.1, 0.2 | 导航组件需要Tailwind环境 |
| 3.1 | 1.1-1.4, 2.1-2.2 | 布局需要基础组件和导航组件 |
| 4.1-4.2 | 3.1 | 认证页面需要布局组件 |
| 5.1-5.4 | 3.1 | 首页需要布局组件 |
| 6.1-6.4 | 3.1 | 论坛模块需要布局组件 |
| 7.1-7.4 | 3.1 | 闲置模块需要布局组件 |
| 8.1-8.3 | 3.1 | 消息模块需要布局组件 |
| 9.1-9.7 | 3.1 | 个人中心需要布局组件 |
| 10.1 | 4.1-4.2, 5.1-5.4, 6.1-6.4, 7.1-7.4, 8.1-8.3, 9.1-9.7 | 移除vant-ui需要所有页面重构完成 |
| 11.1 | 10.1 | 测试需要vant-ui完全移除 |

---

## 页面完整性检查

### 完整文件清单 (27个文件)

#### 布局 (1个) ✅
- [ ] `src/layouts/MainLayout.vue` - 主布局

#### 首页 (4个) ✅
- [ ] `src/views/home/Forum.vue` - 论坛首页
- [ ] `src/views/home/Trade.vue` - 闲置首页
- [ ] `src/views/home/Messages.vue` - 消息首页
- [ ] `src/views/home/Profile.vue` - 个人中心首页

#### 认证 (2个) ✅
- [ ] `src/views/login/index.vue` - 登录页面
- [ ] `src/views/register/index.vue` - 注册页面

#### 论坛模块 (4个) ✅
- [ ] `src/views/forum/index.vue` - 论坛列表
- [ ] `src/views/forum/detail/index.vue` - 帖子详情
- [ ] `src/views/forum/create/index.vue` - 发布帖子
- [ ] `src/views/forum/components/ForumList.vue` - 论坛列表组件

#### 闲置模块 (4个) ✅
- [ ] `src/views/trade/index.vue` - 闲置列表
- [ ] `src/views/trade/detail/index.vue` - 商品详情
- [ ] `src/views/trade/create/index.vue` - 发布商品
- [ ] `src/views/trade/components/TradeList.vue` - 闲置列表组件

#### 消息模块 (3个) ✅
- [ ] `src/views/messages/index.vue` - 消息列表
- [ ] `src/views/messages/chat/index.vue` - 聊天页面
- [ ] `src/views/messages/components/MessageList.vue` - 消息列表组件

#### 个人中心 (7个) ✅
- [ ] `src/views/profile/index.vue` - 个人中心首页
- [ ] `src/views/profile/edit/index.vue` - 编辑资料
- [ ] `src/views/profile/posts/index.vue` - 我的帖子
- [ ] `src/views/profile/items/index.vue` - 我的闲置
- [ ] `src/views/profile/collections/index.vue` - 我的收藏
- [ ] `src/views/profile/messages/index.vue` - 我的消息
- [ ] `src/views/home/components/MyProfile.vue` - 首页个人组件

#### 其他 (2个) ✅
- [ ] `src/App.vue` - 应用入口
- [ ] `src/views/home/index.vue` - 首页路由

### 组件清单 (6个新建)

#### 基础组件 (4个) ✅
- [ ] `src/components/base/Button.vue` - 按钮组件
- [ ] `src/components/base/Input.vue` - 输入框组件
- [ ] `src/components/base/Card.vue` - 卡片组件
- [ ] `src/components/base/Cell.vue` - 单元格组件

#### 导航组件 (2个) ✅
- [ ] `src/components/navigation/NavBar.vue` - 导航栏组件
- [ ] `src/components/navigation/TabBar.vue` - 底部标签栏组件

---

## 质量检查清单

### 代码质量检查

#### 组件代码质量
- [ ] **命名规范**: 组件使用PascalCase命名
- [ ] **Props类型**: 所有props有明确的TypeScript类型定义
- [ ] **无console.log**: 生产代码中无console.log调试语句
- [ ] **无any类型**: 避免使用any类型,使用明确类型
- [ ] **组件单一职责**: 每个组件只做一件事

#### 样式代码质量
- [ ] **使用Tailwind**: 所有样式使用Tailwind类名
- [ ] **无内联样式**: 避免使用内联style属性
- [ ] **响应式断点**: 正确使用响应式前缀
- [ ] **颜色使用变量**: 使用设计系统定义的颜色

#### TypeScript代码质量
- [ ] **类型定义**: 复杂逻辑有类型定义
- [ ] **接口分离**: 大型接口拆分为小接口
- [ ] **无类型断言滥用**: 避免as any,as unknown
- [ ] **枚举使用**: 使用枚举代替魔法数字

### 功能测试检查

#### 交互测试
- [ ] **按钮点击**: 所有按钮点击有视觉反馈
- [ ] **输入框**: 输入框聚焦时有边框高亮
- [ ] **列表项**: 列表项点击有active状态
- [ ] **TabBar**: 底部导航高亮正确

#### 表单测试
- [ ] **输入验证**: 表单提交前有验证
- [ ] **键盘弹出**: 输入框聚焦时键盘不遮挡
- [ ] **提交成功**: 表单提交后有成功提示
- [ ] **提交失败**: 表单提交失败有错误提示

#### 页面测试
- [ ] **页面加载**: 页面加载无白屏
- [ ] **页面跳转**: 路由跳转正常,无404
- [ ] **页面返回**: 返回按钮正常工作
- [ ] **页面刷新**: 刷新页面保持状态

### 移动端专项检查

#### 触摸体验
- [ ] **点击区域**: 所有可点击元素≥44×44px
- [ ] **触摸反馈**: 点击有明显的视觉反馈
- [ ] **手势支持**: 列表支持滑动操作(如果需要)

#### 适配测试
- [ ] **320px**: iPhone SE宽度显示正常
- [ ] **390px**: iPhone 14/15标准宽度显示正常
- [ ] **430px**: iPhone Pro Max宽度显示正常
- [ ] **安全区**: iPhone底部安全区正确适配

---

## 回滚策略

### 回滚场景

#### 场景1: 单个文件重构出错

```bash
# 查看该文件的重构前版本
git show f1fc36d:src/views/login/index.vue > backup_login.vue

# 回滚单个文件
git checkout HEAD -- src/views/login/index.vue
```

#### 场景2: 整个阶段重构出错

```bash
# 查看提交历史
git log --oneline --graph

# 回滚到特定阶段之前
git checkout 15cc75f  # 回滚到添加项目文件的状态
```

#### 场景3: 需要完全回退到重构前

```bash
# 方法1: 回退到初始提交
git checkout f1fc36d

# 方法2: 创建新分支保留重构版本
git branch backup-refactor-remove-vant-ui
git checkout master

# 查看重构分支的备份
git checkout backup-refactor-remove-vant-ui
```

### 备份策略

#### 自动备份 (每次重构前)

```bash
# 在开始重构前,创建备份标签
git tag backup-before-phase-1

# 重构完成后,更新标签
git tag -d backup-before-phase-1
git tag backup-before-phase-1
```

#### 手动备份 (关键节点)

```bash
# 在每个阶段完成后创建备份
git tag phase-1-complete
git tag phase-2-complete
# ...以此类推
```

### 恢复流程

#### 从标签恢复

```bash
# 查看所有备份标签
git tag -l | grep backup

# 从标签恢复
git checkout backup-before-phase-3
```

#### 从分支恢复

```bash
# 查看备份分支
git branch -a | grep backup

# 切换到备份分支
git checkout backup-refactor-remove-vant-ui
```

### 安全规则

1. **每日备份**: 每天开始重构前创建标签
2. **阶段备份**: 每个主要阶段完成后创建标签
3. **测试验证**: 回滚后验证功能正常
4. **文档记录**: 记录回滚原因和解决方法

---

## 执行方式

请运行以下命令开始执行重构：

```bash
/start-work
```
