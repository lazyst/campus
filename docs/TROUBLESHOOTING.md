# 故障排查

## 概述

本文档收集了校园互助平台在开发、测试和生产环境中可能遇到的常见问题及其解决方案。按照问题类型分类，便于快速定位和解决问题。

## 常见问题分类

- [后端问题](#后端问题)
- [前端问题](#前端问题)
- [数据库问题](#数据库问题)
- [Redis 问题](#redis-问题)
- [Docker Swarm 部署问题](#docker-swarm-部署问题)
- [生产环境问题](#生产环境问题)
- [性能问题](#性能问题)

---

## 后端问题

### 问题 1：后端启动失败

**症状**：执行 `mvn spring-boot:run` 后报错，无法启动

**排查步骤**：

1. **检查端口占用**

   ```bash
   # Windows
   netstat -ano | findstr 8080

   # Linux/macOS
   lsof -i:8080
   ```

2. **检查 Java 版本**

   ```bash
   java -version
   # 要求 Java 17 或更高版本
   ```

3. **检查 Maven 依赖**

   ```bash
   cd backend
   mvn clean install -DskipTests
   ```

4. **查看详细错误日志**

   ```bash
   mvn spring-boot:run 2>&1 | tail -100
   ```

**解决方案**：

- 如果端口被占用，停止占用进程或修改 `server.port` 配置
- 确保使用 Java 17：`export JAVA_HOME=/path/to/java-17`
- 重新下载依赖：`mvn clean install -U`

---

### 问题 2：数据库连接失败

**症状**：`Connection to mysql:3306 refused` 或 `Communications link failure`

**排查步骤**：

1. **检查 MySQL 服务状态**

   ```bash
   # Docker 环境下
   docker ps | grep mysql

   # 本地 MySQL
   systemctl status mysql  # Linux
   brew services list | grep mysql  # macOS
   ```

2. **检查连接配置**

   ```bash
   # 测试 MySQL 连接
   mysql -h localhost -uroot -p123 -e "SHOW DATABASES;"
   ```

3. **检查网络连通性**

   ```bash
   # ping 测试
   ping localhost

   # 端口测试
   telnet localhost 3306
   ```

4. **检查防火墙设置**

   ```bash
   # Windows
   netsh firewall show all

   # Linux
   firewall-cmd --list-ports
   ```

**解决方案**：

- 确保 MySQL 服务已启动
- 检查 `application-dev.yml` 中的数据库配置是否正确
- 确保远程服务器的 MySQL 端口已开放
- 验证用户名和密码是否正确

---

### 问题 3：Redis 连接失败

**症状**：`Redis connection refused` 或 `Unable to connect to Redis`

**排查步骤**：

1. **检查 Redis 服务状态**

   ```bash
   # Docker 环境下
   docker ps | grep redis

   # 测试连接
   redis-cli -h localhost -p 6379 -a 123 ping
   ```

2. **检查密码配置**

   ```bash
   # 测试带密码连接
   redis-cli -h localhost -p 6379 -a 123 ping
   # 应该返回 PONG
   ```

3. **检查 Redis 日志**

   ```bash
   docker logs campus-redis
   ```

**解决方案**：

- 确保 Redis 服务已启动
- 检查 `application.yml` 中的 Redis 密码配置
- 确认 Redis 端口已开放

---

### 问题 4：JWT Token 验证失败

**症状**：API 返回 `401 Unauthorized` 或 `Token 已过期`

**排查步骤**：

1. **检查 Token 格式**

   ```
   Authorization: Bearer <your-token>
   ```

2. **检查 Token 是否过期**

   ```java
   // 在代码中打印 Token 过期时间
   Claims claims = Jwts.parser()
       .setSigningKey(jwtSecret)
       .parseClaimsJws(token)
       .getBody();
   System.out.println("过期时间: " + claims.getExpiration());
   ```

3. **检查 JWT Secret 配置**

   - 确认前后端 JWT Secret 一致
   - 确保 Secret 长度足够（至少 256 位）

**解决方案**：

- 重新登录获取新 Token
- 检查 JWT Secret 配置是否正确
- 调整 Token 过期时间

---

### 问题 5：WebSocket 连接失败

**症状**：聊天功能无法使用，WebSocket 连接报错

**排查步骤**：

1. **检查 WebSocket 端点**

   ```
   ws://localhost:8080/ws
   ```

2. **检查浏览器控制台错误**

   ```javascript
   // 浏览器控制台
   const socket = new SockJS('/ws');
   const stompClient = Stomp.over(socket);
   ```

3. **检查后端 WebSocket 配置**

   ```bash
   docker logs campus-backend | grep WebSocket
   ```

**解决方案**：

- 确保后端 WebSocket 配置正确
- 检查跨域配置是否允许前端域名
- 确保防火墙开放 WebSocket 端口

---

## 前端问题

### 问题 1：前端启动失败

**症状**：`npm run dev` 报错或卡住

**排查步骤**：

1. **检查 Node.js 版本**

   ```bash
   node --version
   # 要求 Node.js 18 或更高版本
   ```

2. **检查依赖安装**

   ```bash
   # 删除 node_modules 重新安装
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **检查端口占用**

   ```bash
   # Windows
   netstat -ano | findstr 3000

   # Linux/macOS
   lsof -i:3000
   ```

4. **查看详细错误**

   ```bash
   npm run dev -- --verbose
   ```

**解决方案**：

- 确保 Node.js 版本满足要求
- 清除缓存重新安装依赖
- 停止占用端口的进程

---

### 问题 2：API 请求跨域错误

**症状**：`Access-Control-Allow-Origin` 错误

**排查步骤**：

1. **检查 Vite 代理配置**

   ```javascript
   // vite.config.js
   server: {
     proxy: {
       '/api': 'http://localhost:8080',
       '/ws': {
         target: 'ws://localhost:8080',
         ws: true
       }
     }
   }
   ```

2. **检查后端 CORS 配置**

   ```java
   @Configuration
   public class WebConfig implements WebMvcConfigurer {
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           registry.addMapping("/**")
               .allowedOriginPatterns("*")
               .allowedMethods("GET", "POST", "PUT", "DELETE")
               .allowCredentials(true);
       }
   }
   ```

**解决方案**：

- 确保 Vite 代理配置正确
- 确保后端 CORS 配置包含前端域名
- 检查是否有多个 CORS 配置冲突

---

### 问题 3：Tailwind CSS 样式不生效

**症状**：页面样式显示异常或缺少样式

**排查步骤**：

1. **检查 Tailwind 配置**

   ```javascript
   // tailwind.config.js
   module.exports = {
     content: [
       "./index.html",
       "./src/**/*.{vue,js,ts,jsx,tsx}",
     ],
     theme: {
       extend: {},
     },
     plugins: [],
   }
   ```

2. **检查 CSS 引入**

   ```javascript
   // main.js
   import './styles/main.css'
   ```

3. **重新构建**

   ```bash
   npm run build
   ```

**解决方案**：

- 确保 `tailwind.config.js` 配置正确
- 确保 CSS 文件正确引入
- 清除缓存重新构建

---

### 问题 4：Pinia 状态丢失

**症状**：用户登录状态丢失或数据异常

**排查步骤**：

1. **检查 Pinia 配置**

   ```javascript
   // main.js
   import { createPinia } from 'pinia'
   const pinia = createPinia()
   app.use(pinia)
   ```

2. **检查持久化配置**

   ```javascript
   // stores/user.js
   import { defineStore } from 'pinia'
   import { ref } from 'vue'

   export const useUserStore = defineStore('user', () => {
     const user = ref(null)
     // 持久化逻辑
     return { user }
   })
   ```

**解决方案**：

- 确保 Pinia 正确安装和配置
- 检查状态持久化逻辑
- 清除浏览器缓存重新测试

---

## 数据库问题

### 问题 1：表不存在

**症状**：`Table 'campus_fenbushi.xxx' doesn't exist`

**排查步骤**：

1. **检查数据库是否创建**

   ```sql
   SHOW DATABASES;
   USE campus_fenbushi;
   SHOW TABLES;
   ```

2. **检查初始化脚本**

   ```bash
   mysql -h localhost -uroot -p123 campus_fenbushi < backend/sql/init.sql
   ```

3. **检查表结构**

   ```sql
   DESCRIBE post;
   ```

**解决方案**：

- 执行数据库初始化脚本
- 检查 `init.sql` 文件是否存在且正确
- 手动创建缺失的表

---

### 问题 2：数据插入失败

**症状**：`Duplicate entry` 或 `Cannot add or update a child row`

**排查步骤**：

1. **检查外键约束**

   ```sql
   -- 查看表的外键
   SELECT CONSTRAINT_NAME, COLUMN_NAME
   FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
   WHERE TABLE_NAME = 'post';
   ```

2. **检查唯一索引**

   ```sql
   -- 查看表的索引
   SHOW INDEX FROM post;
   ```

**解决方案**：

- 确保关联数据已存在
- 检查是否违反唯一约束
- 删除重复数据后重试

---

### 问题 3：数据库连接池耗尽

**症状**：`Too many connections` 或连接超时

**排查步骤**：

1. **检查连接数**

   ```sql
   SHOW STATUS LIKE 'Threads_connected';
   SHOW PROCESSLIST;
   ```

2. **检查连接池配置**

   ```yaml
   # application.yml
   spring:
     datasource:
       druid:
         max-active: 20
         min-idle: 5
   ```

**解决方案**：

- 增加连接池大小
- 检查是否有连接未释放
- 重启数据库服务

---

## Redis 问题

### 问题 1：Redis 内存不足

**症状**：`OOM command not allowed when used memory > 'maxmemory'`

**排查步骤**：

1. **检查内存使用**

   ```bash
   redis-cli -h localhost -a 123 info memory
   ```

2. **检查最大内存配置**

   ```bash
   redis-cli -h localhost -a 123 config get maxmemory
   ```

**解决方案**：

- 增加 Redis 内存限制
- 清理无用数据：`FLUSHDB`
- 配置淘汰策略：`maxmemory-policy allkeys-lru`

---

### 问题 2：Redis 缓存未命中

**症状**：缓存数据不存在，每次都查询数据库

**排查步骤**：

1. **检查缓存数据**

   ```bash
   redis-cli -h localhost -a 123 keys "*"
   redis-cli -h localhost -a 123 get <key>
   ```

2. **检查缓存逻辑**

   ```java
   // 确保缓存逻辑正确
   public User getUserById(Long id) {
       String key = "user:" + id;
       User user = redisTemplate.opsForValue().get(key);
       if (user == null) {
           user = userMapper.selectById(id);
           redisTemplate.opsForValue().set(key, user, 24, TimeUnit.HOURS);
       }
       return user;
   }
   ```

**解决方案**：

- 检查缓存 key 命名是否一致
- 检查缓存过期时间
- 检查缓存序列化配置

---

## Docker 部署问题

### 问题 1：容器启动失败

**症状**：`docker-compose up -d` 报错

**排查步骤**：

1. **检查 Docker 服务状态**

   ```bash
   systemctl status docker
   docker version
   ```

2. **检查配置文件**

   ```bash
   docker-compose config
   docker-compose ps -a
   ```

3. **查看详细日志**

   ```bash
   docker-compose logs --tail=100
   ```

**解决方案**：

- 确保 Docker 服务正常运行
- 修复配置文件语法错误
- 检查端口是否被占用

---

### 问题 2：容器间网络不通

**症状**：后端无法连接数据库或 Redis

**排查步骤**：

1. **检查 Docker 网络**

   ```bash
   docker network ls
   docker network inspect campus-fenbushi_default
   ```

2. **检查容器网络配置**

   ```bash
   docker exec -it campus-backend-1 ping mysql
   docker exec -it campus-backend-1 ping redis
   ```

**解决方案**：

- 重新创建网络：`docker-compose down && docker-compose up -d`
- 检查 `docker-compose.yml` 中的网络配置
- 确保容器在同一网络

---

### 问题 3：数据卷挂载失败

**症状**：数据持久化失败或数据丢失

**排查步骤**：

1. **检查数据卷**

   ```bash
   docker volume ls
   docker volume inspect campus-fenbushi_mysql-data
   ```

2. **检查挂载路径**

   ```bash
   docker inspect campus-mysql
   ```

**解决方案**：

- 确保挂载目录存在且有写权限
- 使用命名数据卷替代绑定挂载
- 定期备份数据

---

## 性能问题

### 问题 1：API 响应慢

**症状**：接口响应时间超过 3 秒

**排查步骤**：

1. **检查数据库慢查询**

   ```sql
   -- 查看慢查询日志
   SHOW VARIABLES LIKE 'slow_query_log';
   SHOW VARIABLES LIKE 'long_query_time';
   ```

2. **检查 N+1 查询**

   ```java
   // 避免 N+1 查询
   @Select("SELECT * FROM post WHERE user_id = #{userId}")
   List<Post> selectByUserId(Long userId);

   // 使用懒加载或批量查询
   ```

3. **检查缓存命中率**

   ```bash
   redis-cli -h localhost -a 123 info stats | grep keyspace_hits
   ```

**解决方案**：

- 添加必要的数据库索引
- 使用批量查询替代循环查询
- 增加缓存减少数据库查询

---

### 问题 2：内存使用过高

**症状**：服务器内存不足，应用变慢或崩溃

**排查步骤**：

1. **检查 JVM 内存配置**

   ```bash
   # 查看 JVM 内存使用
   jps
   jstat -gc <pid>
   ```

2. **检查 Docker 内存限制**

   ```bash
   docker stats
   ```

**解决方案**：

- 调整 JVM 堆内存大小：`-Xmx2g -Xms1g`
- 增加 Docker 容器内存限制
- 优化代码减少内存泄漏

---

## 日志位置

### 后端日志

| 日志类型 | 位置 | 查看方式 |
|----------|------|----------|
| 控制台日志 | 控制台输出 | `mvn spring-boot:run` |
| 文件日志 | `logs/` 目录 | `tail -f logs/app.log` |
| Docker 日志 | 容器日志 | `docker logs campus-backend` |

### 前端日志

| 日志类型 | 位置 | 查看方式 |
|----------|------|----------|
| 开发日志 | 浏览器控制台 | F12 打开开发者工具 |
| 构建日志 | 终端输出 | `npm run build` |

### 数据库日志

| 日志类型 | 位置 | 查看方式 |
|----------|------|----------|
| MySQL 错误日志 | Docker 容器内 | `docker logs campus-mysql` |
| MySQL 慢查询日志 | Docker 容器内 | `docker exec campus-mysql mysql -e "SHOW SLOW LOGS;"` |

### Redis 日志

| 日志类型 | 位置 | 查看方式 |
|----------|------|----------|
| Redis 日志 | Docker 容器内 | `docker logs campus-redis` |

## 常用诊断命令

### 后端诊断

```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查 Java 进程
jps -l

# 检查内存使用
jstat -gc <pid>

# 线程堆栈
jstack <pid>
```

### 数据库诊断

```bash
# 检查 MySQL 连接
mysql -h localhost -uroot -p123 -e "SHOW PROCESSLIST;"

# 检查表状态
mysql -h localhost -uroot -p123 -e "CHECK TABLE post;"

# 优化表
mysql -h localhost -uroot -p123 -e "OPTIMIZE TABLE post;"
```

### Redis 诊断

```bash
# 检查连接
redis-cli -h localhost -a 123 ping

# 检查内存
redis-cli -h localhost -a 123 info memory

# 检查慢查询
redis-cli -h localhost -a 123 slowlog get 10
```

### Docker 诊断

```bash
# 检查容器状态
docker-compose ps

# 检查资源使用
docker stats

# 检查磁盘使用
docker system df

# 清理资源
docker system prune -af
```

## 联系支持

如果在排查过程中遇到无法解决的问题，请：

1. **收集信息**：记录错误信息、日志、操作步骤
2. **搜索已有问题**：查看 GitHub Issues 是否已有类似问题
3. **创建新 Issue**：提供详细的问题描述和复现步骤

---

## 生产环境问题

### 问题 1：页面数据加载失败 (502/500错误)

**症状**：前端页面无法加载数据

**排查步骤**：

1. **检查Nginx配置**
   ```bash
   # 使用ssh-mcp连接服务器
   docker exec campus-nginx nginx -T | grep proxy_pass
   ```

2. **检查后端容器状态**
   ```bash
   docker ps | grep backend
   docker logs campus-backend-1 --tail 50
   ```

3. **检查后端健康状态**
   ```bash
   curl http://localhost:8080/api/health
   ```

**解决方案**：
- 确保使用Docker服务名而非固定IP：`proxy_pass http://campus-backend-1:8080`
- 重启Nginx：`docker restart campus-nginx`

---

### 问题 2：中文乱码

**症状**：API返回数据中文显示乱码

**排查步骤**：

1. **检查MySQL字符集配置**
   ```bash
   docker exec campus-mysql mysql -uroot -p123 -e "SHOW VARIABLES LIKE 'character_set%';"
   ```

2. **检查数据库数据**
   ```bash
   docker exec campus-mysql mysql -uroot -p123 campus_fenbushi -e "SELECT id, name FROM board;"
   ```

**解决方案**：
- 添加字符集配置文件：
   ```bash
   docker cp mysql-charset.cnf campus-mysql:/etc/mysql/conf.d/charset.cnf
   docker restart campus-mysql
   ```
- 修复乱码数据：
   ```bash
   docker exec campus-mysql mysql -uroot -p123 campus_fenbushi -e "UPDATE board SET name='正确中文' WHERE id=1;"
   ```

---

## 联系方式

- 项目文档：[DEPLOYMENT_PROD.md](./DEPLOYMENT_PROD.md)
