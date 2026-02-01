# 校园互助平台数据库设计文档

## 一、数据库设计规范

### 1.1 命名规范

数据库对象命名遵循统一的规范，确保命名的一致性和可读性。良好的命名规范可以提高代码的可维护性，减少开发过程中的理解成本。

表名使用小写字母和下划线组成的 snake_case 格式，采用复数形式表示数据集合。例如：用户表命名为 `user`，帖子表命名为 `post`，评论表命名为 `comment`。表名应该清晰表达表的内容和用途，避免使用缩写或模糊的名称。

字段名同样采用 snake_case 格式，使用小写字母和下划线。字段名应该准确描述字段的含义，避免使用保留字或与类型相关的名称。主键字段统一命名为 `id`，外键字段使用被引用表名单数形式加 `_id` 后缀，如 `user_id`、`post_id`。

主键字段统一命名为 `id`，类型为 BIGINT，使用自增策略生成。时间字段使用标准命名：`created_at` 表示记录创建时间，`updated_at` 表示最后更新时间。软删除字段使用 `deleted`，类型为 TINYINT(1)，0 表示未删除，1 表示已删除。

### 1.2 字符集与排序规则

数据库、表和字段的字符集统一使用 `utf8mb4`，这是 MySQL 对完整 Unicode 支持的字符集。`utf8mb4` 字符集支持所有 Unicode 字符，包括 Emoji 表情符号和部分特殊符号，满足现代应用对文本内容的全部需求。

排序规则统一使用 `utf8mb4_unicode_ci`，这是基于 Unicode 标准的排序规则，提供准确的字符比较和排序结果。`_ci` 后缀表示大小写不敏感（Case Insensitive），便于用户搜索和匹配。排序规则的选择影响查询结果的排序顺序和大小写比较行为。

字符集配置需要在数据库创建时指定，也可以在表和字段级别单独配置。为确保整个数据链路的字符编码一致性，建议在数据库级别统一配置。以下是推荐的数据库创建语句：

```sql
CREATE DATABASE campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 1.3 主键策略

主键采用自增策略，使用 BIGINT AUTO_INCREMENT 类型。自增主键的优点是插入性能好、空间占用小、主键连续性好。对于校园互助平台的并发量级，自增主键完全可以满足需求，且具有较好的性能表现。

对于需要分布式场景或数据迁移场景，可以考虑使用雪花算法（Snowflake）生成分布式 ID。雪花算法生成的 ID 具有时间趋势递增、包含机器信息、支持高并发等优点。但当前项目规模下，自增主键已经足够使用，且实现简单、维护成本低。

主键不允许修改，不允许为空，每张表必须有且只有一个主键。复合主键仅在关联表场景使用，单表场景统一使用单字段主键。

## 二、ER 图说明

### 2.1 核心实体

系统包含以下核心业务实体，每个实体对应数据库中的一张表。实体之间通过外键建立关联关系，形成完整的业务数据模型。

用户实体（User）是系统的核心实体，代表平台注册用户。用户包含基本属性（用户名、昵称、密码、手机号）和扩展属性（头像、简介、性别）。用户可以发布帖子、评论、闲置物品，与其他用户进行私信交流。系统通过用户表管理所有用户信息。

板块实体（Board）代表论坛的讨论板块，用于组织不同主题的帖子。板块包含名称、描述、图标、排序等属性。帖子必须属于某个板块，用户通过板块浏览和筛选帖子。板块由管理员创建和管理，普通用户只能浏览。

帖子实体（Post）代表用户发布的论坛帖子，是论坛功能的核心数据。帖子包含标题、正文、浏览量、点赞数、评论数等属性。帖子与用户是一对多关系，一个用户可以发布多篇帖子。帖子与板块是多对一关系，每个帖子属于一个板块。

评论实体（Comment）代表帖子的评论内容，支持多级评论结构。评论包含内容、关联的帖子ID、回复的评论ID等属性。评论与帖子是一对多关系，与用户是一对多关系。评论支持回复功能，通过 `parent_id` 字段建立父子关系。

闲置物品实体（Item）代表用户发布的二手交易物品。闲置包含标题、描述、价格、分类、图片、状态等属性。闲置与用户是一对多关系。闲置物品有独立的状态流转机制（上架、下架、交易完成）。

会话实体（Conversation）代表用户之间的私信会话。会话包含两个参与用户的ID、最后消息内容、最后消息时间等属性。会话与会话中的消息是一对多关系。

消息实体（Message）代表会话中的单条私信消息。消息包含发送方、接收方、内容、已读状态等属性。消息与会话是多对一关系。

### 2.2 关联关系

系统实体之间存在以下关联关系，这些关系通过外键约束进行维护，确保数据的参照完整性。

用户与帖子之间是一对多关系（One-to-Many）。一个用户可以发布多篇帖子，一篇帖子属于一个用户。外键 `user_id` 建立帖子表到用户表的关联。级联策略设置为 RESTRICT，不允许删除有关联帖子的用户。

用户与评论之间是一对多关系。一个用户可以发布多条评论，一条评论属于一个用户。外键 `user_id` 建立评论表到用户表的关联。

用户与闲置之间是一对多关系。一个用户可以发布多条闲置信息，一条闲置属于一个用户。外键 `user_id` 建立闲置表到用户表的关联。

帖子与评论之间是一对多关系。一篇帖子可以包含多条评论，一条评论属于一篇帖子。外键 `post_id` 建立评论表到帖子表的关联。级联策略设置为 CASCADE，删除帖子时自动删除关联评论。

用户与用户之间通过会话实现多对多关系（Many-to-Many）。两个用户之间可以有多个会话（不同交易场景），一个用户可以与多个其他用户会话。会话表包含 `user_id` 和 `target_user_id` 两个外键。

板块与帖子之间是一对多关系。一个板块可以包含多篇帖子，一篇帖子属于一个板块。外键 `board_id` 建立帖子表到板块表的关联。

## 三、核心表结构详解

### 3.1 用户表（user）

用户表存储平台所有用户的基本信息，是系统最核心的数据表之一。

```sql
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(20) NOT NULL COMMENT '用户名，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `nickname` VARCHAR(20) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `bio` VARCHAR(200) NULL COMMENT '个人简介',
    `gender` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_at` DATETIME NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户唯一标识 |
| username | VARCHAR(20) | NOT NULL, UNIQUE | 用户名，4-20位字母数字 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密后的密码 |
| nickname | VARCHAR(20) | NOT NULL | 昵称，2-20位 |
| phone | VARCHAR(11) | NOT NULL, UNIQUE | 手机号，11位数字 |
| avatar | VARCHAR(255) | NULL | 头像存储 URL |
| bio | VARCHAR(200) | NULL | 个人简介，最大200字 |
| gender | TINYINT(1) | NOT NULL, DEFAULT 0 | 性别：0-未知，1-男，2-女 |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 账户状态：0-禁用，1-正常 |
| last_login_at | DATETIME | NULL | 最后登录时间 |
| created_at | DATETIME | NOT NULL | 记录创建时间 |
| updated_at | DATETIME | NOT NULL | 记录更新时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标记 |

### 3.2 板块表（board）

板块表存储论坛讨论板块信息，用于组织帖子的分类。

```sql
CREATE TABLE `board` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    `name` VARCHAR(30) NOT NULL COMMENT '板块名称',
    `description` VARCHAR(100) NOT NULL COMMENT '板块描述',
    `icon` VARCHAR(255) NULL COMMENT '板块图标URL',
    `post_count` INT NOT NULL DEFAULT 0 COMMENT '帖子数量',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 板块唯一标识 |
| name | VARCHAR(30) | NOT NULL | 板块名称 |
| description | VARCHAR(100) | NOT NULL | 板块描述 |
| icon | VARCHAR(255) | NULL | 板块图标 URL |
| post_count | INT | NOT NULL, DEFAULT 0 | 板块内帖子总数 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序序号，数值越小越靠前 |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 板块状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 3.3 帖子表（post）

帖子表存储用户发布的论坛帖子信息。

```sql
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `board_id` BIGINT NOT NULL COMMENT '所属板块ID',
    `title` VARCHAR(50) NOT NULL COMMENT '帖子标题',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_board_id` (`board_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 帖子唯一标识 |
| user_id | BIGINT | NOT NULL, FK->user.id | 作者用户ID |
| board_id | BIGINT | NOT NULL, FK->board.id | 所属板块ID |
| title | VARCHAR(50) | NOT NULL | 帖子标题，5-50字 |
| content | TEXT | NOT NULL | 帖子正文内容 |
| view_count | INT | NOT NULL, DEFAULT 0 | 浏览次数 |
| like_count | INT | NOT NULL, DEFAULT 0 | 点赞数量 |
| comment_count | INT | NOT NULL, DEFAULT 0 | 评论数量 |
| collect_count | INT | NOT NULL, DEFAULT 0 | 收藏数量 |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 帖子状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标记 |

### 3.4 评论表（comment）

评论表存储帖子的评论信息，支持多级评论结构。

```sql
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
    `parent_id` BIGINT NULL COMMENT '父评论ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 评论唯一标识 |
| user_id | BIGINT | NOT NULL, FK->user.id | 评论用户ID |
| post_id | BIGINT | NOT NULL, FK->post.id | 所属帖子ID |
| parent_id | BIGINT | NULL, FK->comment.id | 父评论ID，NULL 表示一级评论 |
| content | VARCHAR(500) | NOT NULL | 评论内容，1-500字 |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 评论状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标记 |

### 3.5 点赞表（like）

点赞表存储用户对帖子的点赞记录。

```sql
CREATE TABLE `like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 点赞记录ID |
| user_id | BIGINT | NOT NULL, FK->user.id | 点赞用户ID |
| post_id | BIGINT | NOT NULL, FK->post.id | 被点赞的帖子ID |
| created_at | DATETIME | NOT NULL | 点赞时间 |

### 3.6 收藏表（collect）

收藏表存储用户对帖子的收藏记录。

```sql
CREATE TABLE `collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
```

### 3.7 闲置物品表（item）

闲置物品表存储用户发布的二手交易物品信息。

```sql
CREATE TABLE `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '闲置ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `title` VARCHAR(30) NOT NULL COMMENT '闲置标题',
    `description` VARCHAR(500) NOT NULL COMMENT '闲置描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) NULL COMMENT '原价',
    `category` VARCHAR(20) NOT NULL COMMENT '分类：electronics、books、clothing、furniture、other',
    `images` JSON NULL COMMENT '图片URL数组',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架，2-已交易',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `contact_count` INT NOT NULL DEFAULT 0 COMMENT '联系量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置物品表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 闲置物品唯一标识 |
| user_id | BIGINT | NOT NULL, FK->user.id | 发布者用户ID |
| title | VARCHAR(30) | NOT NULL | 标题，5-30字 |
| description | VARCHAR(500) | NOT NULL | 描述，10-500字 |
| price | DECIMAL(10,2) | NOT NULL | 售价 |
| original_price | DECIMAL(10,2) | NULL | 原价 |
| category | VARCHAR(20) | NOT NULL | 分类标识 |
| images | JSON | NULL | 图片 URL 数组 |
| status | TINYINT(1) | NOT NULL, DEFAULT 1 | 状态 |
| view_count | INT | NOT NULL, DEFAULT 0 | 浏览次数 |
| contact_count | INT | NOT NULL, DEFAULT 0 | 联系次数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT(1) | NOT NULL, DEFAULT 0 | 软删除标记 |

### 3.8 收藏物品表（item_collect）

收藏物品表存储用户对闲置物品的收藏记录。

```sql
CREATE TABLE `item_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '闲置ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏物品表';
```

### 3.9 会话表（conversation）

会话表存储用户之间的私信会话信息。

```sql
CREATE TABLE `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_user_id` BIGINT NOT NULL COMMENT '对方用户ID',
    `last_message` VARCHAR(255) NULL COMMENT '最后消息内容',
    `last_message_at` DATETIME NULL COMMENT '最后消息时间',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_user_id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_last_message_at` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 会话唯一标识 |
| user_id | BIGINT | NOT NULL, FK->user.id | 会话发起用户ID |
| target_user_id | BIGINT | NOT NULL, FK->user.id | 会话对方用户ID |
| last_message | VARCHAR(255) | NULL | 会话中最后一条消息内容 |
| last_message_at | DATETIME | NULL | 最后消息时间 |
| unread_count | INT | NOT NULL, DEFAULT 0 | 当前用户未读消息数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 3.10 消息表（message）

消息表存储会话中的单条私信消息。

```sql
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送方用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';
```

**字段说明**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 消息唯一标识 |
| conversation_id | BIGINT | NOT NULL, FK->conversation.id | 所属会话ID |
| sender_id | BIGINT | NOT NULL, FK->user.id | 发送方用户ID |
| content | VARCHAR(500) | NOT NULL | 消息内容，1-500字 |
| is_read | TINYINT(1) | NOT NULL, DEFAULT 0 | 是否已读 |
| created_at | DATETIME | NOT NULL | 发送时间 |

### 3.11 通知表（notification）

通知表存储用户的系统通知信息。

```sql
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '通知接收用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '通知类型：comment-评论、like-点赞、collect-收藏、system-系统',
    `title` VARCHAR(50) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(200) NOT NULL COMMENT '通知内容',
    `related_id` BIGINT NULL COMMENT '关联业务ID（帖子ID、评论ID等）',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
```

### 3.12 管理员表（admin）

管理员表存储后台管理员账号信息。

```sql
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(20) NOT NULL COMMENT '管理员用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `nickname` VARCHAR(20) NOT NULL COMMENT '管理员昵称',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_at` DATETIME NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';
```

## 四、索引设计

### 4.1 主键索引

主键索引是表的基础索引，基于主键字段自动创建。主键索引是聚簇索引（Clustered Index），数据行按照主键顺序物理存储。主键索引具有最高的查询性能，所有根据主键的查询都能利用索引快速定位。

用户表主键索引：`PRIMARY (id)`

帖子表主键索引：`PRIMARY (id)`

### 4.2 唯一索引

唯一索引保证字段值的唯一性，同时提供索引加速查询的功能。唯一索引允许 NULL 值（如果字段允许），但重复的非 NULL 值会被拒绝。

用户表唯一索引：`uk_username (username)`、`uk_phone (phone)`

管理员表唯一索引：`uk_username (username)`

### 4.3 普通索引

普通索引用于加速查询和排序操作，是最常用的索引类型。普通索引不限制字段值的唯一性，可以包含重复值。

用户表索引：`idx_status (status)`

帖子表索引：`idx_user_id (user_id)`、`idx_board_id (board_id)`、`idx_created_at (created_at)`、`idx_status (status)`

评论表索引：`idx_user_id (user_id)`、`idx_post_id (post_id)`、`idx_parent_id (parent_id)`、`idx_created_at (created_at)`

闲置表索引：`idx_user_id (user_id)`、`idx_category (category)`、`idx_status (status)`、`idx_price (price)`、`idx_created_at (created_at)`

### 4.4 复合索引

复合索引基于多个字段创建，适用于多条件组合查询的场景。复合索引遵循最左前缀原则，查询条件必须包含索引的最左字段才能使用索引。

会话表复合索引：`uk_user_target (user_id, target_user_id)`，加速按用户查询会话列表。

收藏表复合索引：`uk_user_post (user_id, post_id)`，加速按用户查询收藏帖子。

## 五、外键约束说明

系统使用外键约束维护数据的参照完整性，确保关联数据的有效性。外键约束在创建表时定义，在数据操作时自动检查。

用户相关外键：帖子表 `user_id` 引用用户表 `id`，评论表 `user_id` 引用用户表 `id`，闲置表 `user_id` 引用用户表 `id`，消息表 `sender_id` 引用用户表 `id`。

板块相关外键：帖子表 `board_id` 引用板块表 `id`。

帖子相关外键：评论表 `post_id` 引用帖子表 `id`，点赞表 `post_id` 引用帖子表 `id`，收藏表 `post_id` 引用帖子表 `id`。

会话相关外键：消息表 `conversation_id` 引用会话表 `id`。

外键的级联策略根据业务需求设置：删除用户时，关联的帖子、评论、闲置采用 RESTRICT 策略（禁止删除），删除帖子时关联评论采用 CASCADE 策略（级联删除）。

## 六、初始化数据

### 6.1 板块初始数据

系统初始化时创建以下讨论板块：

| ID | 板块名称 | 板块描述 | 排序 |
|----|----------|----------|------|
| 1 | 学习交流 | 学习经验分享、考试资料交流、学习问题讨论 | 1 |
| 2 | 校园生活 | 校园趣事、生活技巧分享、校园活动 | 2 |
| 3 | 二手交易 | 闲置物品交易、求购信息发布 | 3 |
| 4 | 失物招领 | 失物招领、寻物启事 | 4 |
| 5 | 情感树洞 | 情感分享、心事倾诉、互助交流 | 5 |

### 6.2 管理员初始账号

系统初始化时创建以下管理员账号：

| 用户名 | 密码 | 昵称 |
|--------|------|------|
| admin | admin123456 | 超级管理员 |

**注意**：生产环境部署后请立即修改管理员密码，确保账号安全。建议使用强密码，包含大小写字母、数字和特殊符号。

## 七、数据库脚本

### 7.1 创建数据库脚本

```sql
-- 创建数据库
CREATE DATABASE campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE campus;

-- 创建用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(20) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(20) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) NULL COMMENT '头像URL',
    `bio` VARCHAR(200) NULL COMMENT '个人简介',
    `gender` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '性别',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `last_login_at` DATETIME NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 板块表、帖子表、评论表、点赞表、收藏表、闲置物品表、收藏物品表、会话表、消息表、通知表、管理员表...
-- 请参考前述各表结构定义
```

### 7.2 初始化数据脚本

```sql
-- 初始化板块数据
INSERT INTO `board` (`name`, `description`, `icon`, `sort_order`, `status`) VALUES
('学习交流', '学习经验分享、考试资料交流、学习问题讨论', NULL, 1, 1),
('校园生活', '校园趣事、生活技巧分享、校园活动', NULL, 2, 1),
('二手交易', '闲置物品交易、求购信息发布', NULL, 3, 1),
('失物招领', '失物招领、寻物启事', NULL, 4, 1),
('情感树洞', '情感分享、心事倾诉、互助交流', NULL, 5, 1);

-- 初始化管理员账号
-- 密码为 BCrypt 加密后的 'admin123456'
INSERT INTO `admin` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1);
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
