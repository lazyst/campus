# 环境配置指南

## 概述

本文档说明项目的环境配置，包括开发环境和生产环境。

## 服务地址

### 开发环境

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端API | http://localhost:8080 | 本地启动 |
| 用户前端 | http://localhost:3000 | 本地启动 |
| 管理后台 | http://localhost:3001 | 本地启动 |

### 生产环境

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端首页 | http://192.168.100.133 | Nginx |
| 管理后台 | http://192.168.100.133/admin | Nginx |
| 后端API | 通过Nginx代理 | Docker容器 |

## 生产环境部署配置

详见 [DEPLOYMENT_PROD.md](./DEPLOYMENT_PROD.md)

### Docker容器列表

```
campus-nginx          - Nginx反向代理 (80/443)
campus-frontend-user   - 用户前端 (3000)
campus-frontend-admin  - 管理后台 (3001)
campus-backend-1      - 后端API (8080)
campus-backend-2      - 后端API副本
campus-mysql          - MySQL数据库 (3306)
campus-redis          - Redis缓存 (6379)
```

### 验证命令

```bash
# 首页
curl http://192.168.100.133/

# 管理后台
curl http://192.168.100.133/admin/

# API健康检查
curl http://192.168.100.133/api/health

# 板块数据
curl http://192.168.100.133/api/boards
```

## 开发环境配置

详见 [QUICK_START.md](./QUICK_START.md)

## 注意事项

1. **禁止硬编码IP地址**：使用Docker服务名通信
2. **字符集配置**：MySQL必须使用utf8mb4
3. **备份**：定期备份数据库数据
