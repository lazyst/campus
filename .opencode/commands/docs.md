# 项目文档新建/更新

请按照以下步骤为当前项目创建或更新最小化的文档清单：

### 第一步：项目分析
1. 扫描项目：
- 项目类型（语言、框架）
- 依赖管理文件（package.json, requirements.txt, go.mod 等）
- 配置文件（.env, docker-compose.yml 等）
- 基于代码`深度理解`项目
2. 分析目录结构，识别：
- 源代码目录
- 测试目录
- 配置目录
- 脚本目录
### 第二步：文档检查与创建
对以下每个文档进行检查：

- 如果文档不存在：创建新文档
- 如果文档存在：分析内容完整性，决定是否需要更新
#### 必需文档清单：
1. README.md
- 包含：项目简介、技术栈、快速启动、目录结构
- 如果存在但缺少关键内容，补充缺失部分
2. docs/ENV_SETUP.md
- 包含：依赖服务（MySQL/Redis 等）的运行方式
- 必须明确说明：Docker Compose / 本地安装 / 远程服务器
- 包含连接地址、端口、初始化脚本位置
3. docs/ARCHITECTURE.md
- 包含：系统分层、核心流程、技术架构图（用 ASCII 或文本描述）
4. docs/PROJECT_STRUCTURE.md
- 包含：完整目录树及各目录职责说明
5. docs/CODING_STANDARDS.md
- 包含：命名规范、错误处理、安全约束
6. ./AGENTS.md
- 包含：项目概要、AI 工作流指令、相关文档引用
### 第三步：内容生成规则
对每个文档遵循以下规则：

#### 通用规则：
- 使用 Markdown 格式
- 标题层级清晰（# ## ###）
- 代码块使用正确的语言标识
- 优先使用文本而非图片
#### README.md 规则：
```markdown 
# [项目名称]

## 简介
[1-2 句话描述项目目的]

## 技术栈
- 语言：[语言及版本]
- 框架：[框架名称]
- 数据库：[MySQL/Redis 版本及运行方式]

## 快速启动
### 前置条件
- [要求1]
- [要求2]

### 启动步骤
1. [步骤1]
2. [步骤2]

### 验证
[如何验证启动成功]

## 项目结构
[简短说明或链接到 @docs/PROJECT_STRUCTURE.md]

## 文档
- [环境配置](@docs/ENV_SETUP.md)
- [架构设计](@docs/ARCHITECTURE.md)
- [代码规范](@docs/CODING_STANDARDS.md)
```

#### ENV_SETUP.md 规则：
```markdown 
# 环境配置指南

## 依赖服务运行方式
- [x] Docker Compose（推荐）
- [ ] 本地安装
- [x] 远程服务器

## 数据库配置
### MySQL
- 运行方式：[Docker/本地/远程]
- 连接信息：
  - Host: [地址]
  - Port: [端口]
  - Database: [数据库名]
  - User: [用户名]
  - Password: [密码]
  
### Redis
- 运行方式：[Docker/本地/远程]
- 连接信息：
  - Host: [地址]
  - Port: [端口]
  - Password: [密码]

## 初始化
- 数据库初始化脚本：[脚本路径]
- 首次启动步骤：[步骤说明]
```

#### ARCHITECTURE.md 规则：
```markdown
# 系统架构设计

## 技术架构
[分层说明，用文本描述]

## 核心流程
[主要业务流程描述]

## 关键组件
- [组件1]：[职责]
- [组件2]：[职责]
```
#### PROJECT_STRUCTURE.md 规则：
```markdown 
# 项目目录结构

## 目录树
[完整的目录树结构]

## 目录职责
- [目录1]：[职责说明]
- [目录2]：[职责说明]
```

#### CODING_STANDARDS.md 规则：
```markdown
# 代码规范

## 命名规范
- [变量命名]
- [函数命名]
- [文件命名]

## 错误处理
[错误处理规范]

## 安全约束
[安全相关约束]
```

#### AGENTS.md 规则：
```markdown 
# 项目说明

## 项目概要
[一句话项目描述]

## 技术栈
- [语言/框架]

## 开发前必读
在开始开发前，请按顺序阅读：
1. @docs/ENV_SETUP.md
2. @docs/ARCHITECTURE.md  
3. @docs/CODING_STANDARDS.md

## 工作流建议
1. 理解需求
2. 查看相关模块代码
3. 编写/更新测试
4. 实现功能
5. 运行验证

## 常用命令
- 启动：[命令]
- 测试：[命令]
- 构建：[命令]
```

### 第四步：执行与输出
1. 首先输出分析结果：
- 项目类型
- 现有文档状态
- 需要创建/更新的文档列表
2. 对每个需要处理的文档：
- 说明处理原因（创建/更新）
- 展示文档内容
- 询问是否确认写入
3. 获得确认后：
- 创建或更新对应文件
- 报告完成状态

### 第五步：维护部署文档（可选）

当项目需要部署或存在 Docker/服务器配置时，需要创建或维护部署文档。

#### 何时需要：
- 项目有 Docker 配置（Dockerfile、docker-compose.yml）
- 项目有部署脚本或 CI/CD 配置
- 项目需要部署到生产环境或测试环境
- 项目有外部服务依赖（云数据库、云存储等）

#### 必需文档清单：
1. docs/DEPLOYMENT.md
- 包含：部署方式选择（Docker/手动/K8s）
- 包含：服务器环境要求（CPU、内存、端口）
- 包含：部署步骤（详细命令）
- 包含：数据库迁移步骤
- 包含：健康检查和验证命令
- 包含：回滚策略

2. docs/ENVIRONMENT_VARIABLES.md
- 包含：所有环境变量说明
- 包含：开发/测试/生产环境配置差异
- 包含：敏感信息管理方式

3. docs/TROUBLESHOOTING.md
- 包含：常见部署问题
- 包含：日志查看位置
- 包含：故障排查步骤

#### DEPLOYMENT.md 规则：
```markdown
# 部署指南

## 部署方式选择
- [x] Docker Compose（推荐）
- [ ] 手动部署
- [ ] Kubernetes

## 服务器要求
- CPU：[最低要求]
- 内存：[最低要求]
- 磁盘：[空间要求]
- 开放端口：[端口列表]

## 部署步骤

### 1. 环境准备
```bash
# 步骤1
# 步骤2
```

### 2. 获取代码
```bash
git clone <repository-url>
cd <project-directory>
```

### 3. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件
```

### 4. 启动服务
```bash
# Docker 方式
docker-compose up -d

# 或手动方式
./deploy.sh
```

### 5. 验证部署
```bash
# 检查服务状态
docker-compose ps

# 查看健康检查
curl http://localhost:port/health
```

## 数据库迁移
```bash
# 迁移命令
npm run migration:run

# 或手动执行
mysql -u root -p < database/migrations/xxx.sql
```

## 回滚策略
- 回滚命令：[命令]
- 回滚版本：[说明]
```

#### ENVIRONMENT_VARIABLES.md 规则：
```markdown
# 环境变量说明

## 开发环境
| 变量名  | 必填 | 默认值    | 说明       |
| ------- | ---- | --------- | ---------- |
| DB_HOST | 是   | localhost | 数据库地址 |
| DB_PORT | 否   | 3306      | 数据库端口 |

## 测试环境
| 变量名  | 必填 | 默认值 | 说明       |
| ------- | ---- | ------ | ---------- |
| DB_HOST | 是   | -      | 数据库地址 |
| API_URL | 是   | -      | API 地址   |

## 生产环境
| 变量名     | 必填 | 默认值 | 说明                   |
| ---------- | ---- | ------ | ---------------------- |
| DB_HOST    | 是   | -      | 数据库地址（云数据库） |
| REDIS_HOST | 是   | -      | Redis 地址             |
```

#### TROUBLESHOOTING.md 规则：
```markdown
# 故障排查

## 常见问题

### 服务无法启动
**症状**：[描述]
**排查步骤**：
1. 检查日志：`docker-compose logs <service-name>`
2. 检查端口：`netstat -tlnp | grep <port>`
3. 检查配置：`docker-compose config`

### 数据库连接失败
**症状**：[描述]
**排查步骤**：
1. 检查数据库服务状态
2. 检查环境变量配置
3. 检查网络连通性

## 日志位置
- 应用日志：`/var/log/app/`
- Nginx 日志：`/var/log/nginx/`
- Docker 日志：`docker-compose logs`

## 联系支持
[联系信息]
```

#### 维护时机：
- 新增环境变量时
- 修改部署流程时
- 更新依赖服务版本时
- 发现部署问题并解决后

### 开始执行
请现在开始分析当前项目，并按照上述规则创建或更新文档清单。

## 验证文档
- 根据深度理解的项目代码review一遍文档，确保项目代码和文档内容必须一致