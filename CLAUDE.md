# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

校园互助平台全栈项目，包含三个子项目：
- `backend/` - Spring Boot 3.2 后端服务 (端口 8080)
- `frontend-user/` - Vue 3 用户前端 (端口 3000)
- `frontend-admin/` - Vue 3 管理后台 (端口 3001)

## 常用工具

### 后端
| 类 | 用途 |
|----|------|
| `Result<T>` | 统一 API 响应 (`Result.success(data)`, `Result.error(msg)`) |
| `ResultCode` | 响应状态码枚举 (SUCCESS, ERROR, NOT_FOUND, PARAM_ERROR 等) |
| `BaseEntity` | 公共字段 (id, createdAt, updatedAt) |
| `LambdaQueryWrapper<T>` | MyBatis-Plus 类型安全查询 |
| `GlobalExceptionHandler` | 全局异常处理 |

### 前端
| 钩子/仓库 | 用途 |
|----------|------|
| `request` | Axios 实例，自动添加 Token |
| `useUserStore()` | 认证状态管理 (Pinia) |
| `showToast()` | 用户反馈提示 |

---

## 命名规范

### 后端 (Spring Boot)
| 类型 | 规范 | 示例 |
|------|------|------|
| 类 | PascalCase | `PostService`, `UserController` |
| 方法 | camelCase + 动词 | `createPost()`, `getUserById()` |
| 包 | lowercase | `com.campus.modules.auth` |

### 前端 (Vue 3)
| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase.vue | `UserProfile.vue` |
| 变量 | camelCase | `userInfo`, `isLoading` |
| 函数 | handle动词名词() | `handleSubmit()` |
| CSS类 | kebab-case | `.user-profile-card` |

---

## 设计约束

禁止使用：图标(SVG/图片/字体)、Emoji、紫色(#6366F1)/橙色。颜色使用靛蓝色(#1E3A8A)，间距从 `src/styles/design-tokens.css` 获取。

---

## 重要文件

- `AGENTS.md` - 完整编码规范
- `docs/BUSINESS_LOGIC_DIAGRAMS.md` - 业务逻辑与权限矩阵
