# 校园互助平台开发环境搭建指南

## 一、环境要求

### 1.1 必需软件

搭建本地开发环境需要安装以下必需软件，请确保所有软件的版本满足要求后再进行项目配置。

Node.js 是前端开发的基础运行环境，要求版本 18.0 或更高。Node.js 提供了 JavaScript 运行时环境和 npm 包管理器。前端项目依赖 Node.js 运行构建脚本和开发服务器。建议使用 LTS（长期支持）版本，如 Node.js 20.x，以获得更好的稳定性和兼容性。可以通过官方网站下载安装包，或使用 nvm（Node Version Manager）管理多个 Node.js 版本。安装完成后，在终端执行 `node --version` 验证安装是否成功。

Java 是后端开发的基础运行环境，要求版本 17 或更高。Spring Boot 3.2 基于 Java 17 构建，需要使用新版本的 Java 特性。推荐使用 Eclipse Temurin（原 AdoptOpenJDK）或 Amazon Corretto 等开源 JDK 发行版。Oracle JDK 也可以使用，但需要注意许可证条款。安装完成后，在终端执行 `java -version` 验证安装是否成功。

Maven 是 Java 项目的构建工具，要求版本 3.8 或更高。Maven 负责管理项目依赖、执行构建任务、运行测试等。Spring Boot 项目提供了 Maven 插件，简化了构建配置。可以从 Apache Maven 官网下载安装包，或使用包管理器安装。安装完成后，在终端执行 `mvn --version` 验证安装是否成功。

MySQL 是关系型数据库，要求版本 8.0 或更高。系统使用 MySQL 存储所有业务数据。MySQL 8.0 提供了更好的性能和新特性。Windows 用户可以从 MySQL 官网下载安装包，或使用 XAMPP、WAMP 等集成环境。macOS 用户可以使用 Homebrew 安装。Linux 用户可以使用包管理器安装。安装完成后，执行 `mysql --version` 验证安装是否成功。

Git 是版本控制工具，用于代码的版本管理和协作开发。Git 是开发过程中不可或缺的工具，几乎所有的代码托管平台都支持 Git。Windows 用户可以从 Git 官网下载安装包，macOS 和 Linux 用户通常已预装 Git。安装完成后，执行 `git --version` 验证安装是否成功。

### 1.2 推荐工具

除了必需软件外，以下工具可以显著提升开发效率和体验。

IDE 选择方面，后端开发推荐使用 IntelliJ IDEA，这是 Java 开发领域最流行的 IDE，功能强大、插件丰富。IntelliJ IDEA 的 Ultimate 版本对 Spring Boot 和 Web 开发有很好的支持，社区版也可以满足基本需求。前端开发推荐使用 VS Code，这是轻量级但功能强大的代码编辑器，特别适合 Vue.js 开发。VS Code 有丰富的扩展生态，可以配置为完整的前端开发环境。

数据库工具方面，推荐使用 Navicat 或 DBeaver 进行数据库管理和查询。Navicat 提供图形化界面，支持数据库设计、数据导入导出、SQL 执行等功能。DBeaver 是开源免费的数据库管理工具，支持多种数据库类型。这些工具比命令行更直观，便于进行数据库设计和数据查看。

API 测试工具方面，推荐使用 Apifox 或 Postman 进行接口测试。这些工具可以方便地组织 API 测试用例，保存请求历史，生成测试报告。Apifox 是国产工具，支持接口文档管理、自动化测试等功能，与团队协作更方便。Postman 是老牌 API 测试工具，功能全面。

## 二、数据库配置

### 2.1 MySQL 安装与配置

MySQL 的安装过程因操作系统不同而有所差异，以下是各平台的安装指引。

Windows 系统安装 MySQL：首先从 MySQL 官网下载 MySQL Installer。运行安装程序，选择「Developer Default」或「Full」安装类型。安装过程中设置 root 用户密码，建议使用强密码并妥善保管。完成安装后，MySQL 服务会自动启动。可以通过「服务」管理器确认 MySQL 服务状态，或在命令提示符执行 `net start mysql` 启动服务。MySQL 默认监听 3306 端口，确保该端口未被其他程序占用。

macOS 系统安装 MySQL：推荐使用 Homebrew 包管理器安装。执行以下命令安装 MySQL：

```bash
# 安装 Homebrew（如果未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 MySQL
brew install mysql

# 启动 MySQL 服务
brew services start mysql

# 安全配置（可选）
mysql_secure_installation
```

Linux 系统安装 MySQL（以 Ubuntu 为例）：

```bash
# 更新软件包列表
sudo apt update

# 安装 MySQL 服务器
sudo apt install mysql-server

# 启动 MySQL 服务
sudo systemctl start mysql

# 安全配置
sudo mysql_secure_installation
```

### 2.2 创建数据库

安装完成 MySQL 后，需要创建项目使用的数据库。执行以下步骤创建数据库并设置字符集：

```bash
# 登录 MySQL（使用安装时设置的 root 密码）
mysql -u root -p

# 创建数据库
CREATE DATABASE campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 验证数据库创建
SHOW DATABASES;
```

如果需要创建其他数据库用户并授权（生产环境推荐），可以执行以下 SQL：

```sql
-- 创建开发用户
CREATE USER 'campus'@'localhost' IDENTIFIED BY 'campus123456';

-- 授予数据库权限
GRANT ALL PRIVILEGES ON campus.* TO 'campus'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 2.3 导入初始化脚本

项目提供了数据库初始化脚本，包含表结构创建和初始数据导入。初始化脚本位于 `backend/src/main/resources/schema.sql`。

```bash
# 导入初始化脚本（使用 root 用户）
mysql -u root -p campus < backend/src/main/resources/schema.sql

# 或者进入 MySQL 后执行 source 命令
mysql -u root -p
USE campus;
SOURCE backend/src/main/resources/schema.sql;
```

导入完成后，验证数据是否正确导入：

```sql
-- 查看所有表
SHOW TABLES;

-- 查看板块数据
SELECT * FROM board;

-- 查看管理员账号
SELECT * FROM admin;
```

## 三、后端开发环境

### 3.1 Maven 配置

后端项目使用 Maven 管理依赖和构建。首次打开项目时，IDE 会自动下载项目依赖。如果需要手动下载依赖，执行以下命令：

```bash
# 进入后端项目目录
cd backend

# 清理并下载所有依赖
mvn clean install

# 跳过测试加快下载速度
mvn clean install -DskipTests
```

Maven 仓库默认使用中央仓库，国内网络环境可能较慢。可以在 Maven 配置文件中添加阿里云镜像仓库加速下载。编辑 `~/.m2/settings.xml` 文件：

```xml
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>*</mirrorOf>
            <name>Aliyun Maven Mirror</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
```

### 3.2 导入项目

使用 IntelliJ IDEA 导入后端项目：

1. 打开 IntelliJ IDEA，选择「Open」或「Import Project」
2. 选择 `backend` 目录，点击「OK」
3. IDEA 会自动识别为 Maven 项目，开始下载依赖
4. 等待依赖下载完成，可以在右下角查看进度

导入项目后，配置项目 SDK：

1. 点击「File」→「Project Structure」
2. 在「Project」选项卡中，选择已安装的 JDK 17 或更高版本
3. 在「Modules」选项卡中，确认模块的 SDK 和语言级别设置正确

### 3.3 配置数据库连接

项目使用配置文件管理数据库连接，开发环境配置位于 `src/main/resources/application-dev.yml`。确保配置文件中的数据库连接信息正确：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

如果使用其他数据库用户，修改 `username` 和 `password` 配置。连接信息中的时区设置为 `Asia/Shanghai`，确保时间数据正确处理。

### 3.4 启动后端服务

配置完成后，启动后端服务：

**方式一：使用 Maven 命令启动**

```bash
cd backend
mvn spring-boot:run
```

**方式二：使用 IDE 启动**

1. 找到 `CampusApplication.java` 主类
2. 右键点击类名，选择「Run 'CampusApplication'」
3. 首次启动会编译项目，耗时较长

**方式三：打包后运行**

```bash
cd backend
mvn package -DskipTests
java -jar target/campus-backend.jar
```

后端服务启动成功后，控制台会显示以下信息：

```
Started CampusApplication in X.XXX seconds
```

### 3.5 验证后端运行

后端服务启动后，通过以下方式验证服务是否正常运行：

1. 访问 API 根地址：http://localhost:8080/api，应该返回系统信息或 404
2. 访问 Swagger 文档：http://localhost:8080/swagger-ui.html，可以查看所有 API 接口
3. 使用 API 测试工具调用登录接口，验证认证功能正常

如果服务启动失败，检查以下几点：

- MySQL 服务是否已启动
- 数据库连接信息是否正确
- 端口 8080 是否被其他程序占用
- JDK 版本是否为 17 或更高

## 四、前端用户端开发环境

### 4.1 安装 Node.js

如果尚未安装 Node.js，从 Node.js 官网下载并安装。推荐使用 LTS 版本。安装完成后验证版本：

```bash
node --version
npm --version
```

### 4.2 安装依赖

前端项目依赖通过 npm 安装。执行以下命令安装依赖：

```bash
# 进入用户前端项目目录
cd frontend-user

# 安装项目依赖
npm install

# 如果网络较慢，可以使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

依赖安装过程中会创建 `node_modules` 目录，存放所有项目依赖。依赖安装完成后，执行 `npm install` 应该显示 `audited` 状态。

### 4.3 配置环境变量

前端项目使用环境变量配置 API 地址等参数。开发环境配置位于项目根目录的 `.env.development` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
```

根据实际后端服务地址修改配置。如果后端服务运行在不同的主机或端口，需要相应修改这些变量。

### 4.4 启动开发服务器

前端用户端使用 Vite 作为开发服务器，支持热模块替换（HMR），代码修改后自动更新页面，无需刷新：

```bash
cd frontend-user
npm run dev
```

启动成功后，终端显示以下信息：

```
VITE v5.x.x  ready in 300 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

### 4.5 验证前端运行

前端开发服务器启动后，打开浏览器访问 http://localhost:3000，验证页面是否正常显示。如果页面显示异常，检查浏览器控制台是否有错误信息。

常见问题及解决方案：

- **页面空白**：检查 API 地址配置是否正确，后端服务是否启动
- **样式异常**：确认 Tailwind CSS 正确安装，查看控制台 CSS 加载错误
- **接口请求失败**：检查浏览器网络标签页，查看请求是否发送到正确的地址

## 五、前端管理端开发环境

### 5.1 安装依赖

管理前端同样基于 Vue 3 技术栈，依赖安装过程与用户端类似：

```bash
# 进入管理前端项目目录
cd frontend-admin

# 安装项目依赖
npm install

# 使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

### 5.2 配置环境变量

管理前端的环境配置文件为 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

确保 API 地址指向正确的后端服务地址。

### 5.3 启动开发服务器

管理前端的开发服务器运行在 3001 端口，避免与用户端（3000 端口）冲突：

```bash
cd frontend-admin
npm run dev
```

启动成功后，访问地址为 http://localhost:3001。

### 5.4 验证管理端运行

打开浏览器访问 http://localhost:3001，应该显示管理端登录页面。使用初始管理员账号登录验证功能是否正常：

- 用户名：`admin`
- 密码：`admin123456`

## 六、完整开发流程

### 6.1 代码修改热重载

前端开发服务器支持热模块替换（HMR），代码修改后会自动更新页面。CSS 修改可以热更新，无需刷新页面。JavaScript 修改在多数情况下也可以热更新，少数情况需要手动刷新。

后端服务使用 Spring Boot DevTools 支持热重载。修改 Java 代码后，DevTools 会自动重启应用。由于 Java 重启耗时较长，DevTools 会在保存时进行增量编译，减少重启时间。

### 6.2 调试技巧

前端调试推荐使用浏览器开发者工具。Chrome 浏览器的开发者工具功能完善，支持 DOM 检查、网络请求监控、断点调试等。在 Sources 面板可以设置断点调试 JavaScript 代码，在 Network 面板可以查看所有 HTTP 请求。

后端调试可以使用 IDE 的调试功能。在 IntelliJ IDEA 中，可以为方法设置断点，以 Debug 模式启动应用。程序执行到断点时会暂停，可以查看变量值、继续执行或单步调试。

### 6.3 代码检查与格式化

项目配置了 ESLint 进行前端代码检查。执行以下命令检查代码问题：

```bash
# 检查代码问题
npm run lint

# 自动修复可修复的问题
npm run lint -- --fix
```

后端代码使用 Checkstyle 进行检查。执行以下命令检查：

```bash
cd backend
mvn checkstyle:check
```

代码检查会在持续集成流程中自动执行，确保代码质量。建议在提交代码前手动运行检查，提前发现和修复问题。

## 七、常见问题排查

### 7.1 后端启动失败

**问题表现**：执行 `mvn spring-boot:run` 后，控制台报错，提示连接数据库失败或其他错误。

**排查步骤**：

1. 检查 MySQL 服务是否启动
2. 检查数据库用户名和密码是否正确
3. 检查数据库是否已创建
4. 检查端口 8080 是否被占用：`netstat -ano | findstr 8080`
5. 查看详细的错误日志，定位具体问题

**解决方案**：

- 启动 MySQL 服务：`sudo systemctl start mysql`（Linux/macOS）或 `net start mysql`（Windows）
- 检查并修改 `application-dev.yml` 中的数据库配置
- 执行数据库初始化脚本创建表结构

### 7.2 前端启动失败

**问题表现**：执行 `npm run dev` 后，终端报错，无法启动开发服务器。

**排查步骤**：

1. 检查 Node.js 版本是否满足要求：`node --version`
2. 检查依赖是否安装完整：删除 `node_modules` 后重新执行 `npm install`
3. 检查端口是否被占用：3000（用户端）或 3001（管理端）
4. 查看终端输出的错误信息

**解决方案**：

- 升级 Node.js 到 18.0 或更高版本
- 使用淘宝镜像加速依赖下载：`npm install --registry=https://registry.npmmirror.com`
- 杀死占用端口的进程，或修改 Vite 配置使用其他端口

### 7.3 数据库连接失败

**问题表现**：后端启动正常，但 API 请求返回数据库连接错误。

**排查步骤**：

1. 检查 MySQL 服务状态
2. 检查数据库连接配置
3. 检查数据库用户权限
4. 检查 MySQL 允许的主机连接

**解决方案**：

- 确保 MySQL 服务正在运行
- 检查 `application-dev.yml` 中的数据库 URL、用户名、密码
- 授权数据库用户访问权限：`GRANT ALL PRIVILEGES ON campus.* TO 'user'@'localhost';`
- 检查 MySQL 配置文件是否允许 localhost 连接

### 7.4 API 请求跨域错误

**问题表现**：前端页面调用 API 时，浏览器报错「Access-Control-Allow-Origin」等跨域错误。

**排查步骤**：

1. 检查后端是否正确配置 CORS
2. 检查 Nginx 反向代理配置
3. 检查请求的 URL 是否正确

**解决方案**：

- 确保后端已配置 CORS 跨域支持
- 检查请求头中的 Origin 是否在允许列表中
- 如果使用 Nginx 代理，确保代理配置正确

### 7.5 热更新不生效

**问题表现**：修改前端代码后，页面没有自动更新。

**排查步骤**：

1. 检查终端是否显示「HMR 重新连接」或错误信息
2. 检查浏览器控制台是否有错误
3. 检查是否为不支持热更新的修改（如新增路由）

**解决方案**：

- 刷新浏览器页面
- 重启开发服务器
- 检查是否修改了入口文件或路由配置（需要重启）

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
