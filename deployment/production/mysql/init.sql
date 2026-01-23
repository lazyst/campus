-- =====================================================
-- 校园互助平台数据库初始化脚本
-- Database: campus
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号（唯一）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `gender` TINYINT DEFAULT 1 COMMENT '性别：0未知 1男 2女',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0封禁 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认用户（测试用）
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `gender`, `status`) VALUES
(1, '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 1, 1);

-- =====================================================
-- 2. 管理员表 (admin)
-- =====================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 2 COMMENT '角色：1超级管理员 2普通管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 插入默认管理员（密码：admin123）
INSERT INTO `admin` (`id`, `username`, `password`, `nickname`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 1, 1);

-- =====================================================
-- 3. 板块表 (board)
-- =====================================================
DROP TABLE IF EXISTS `board`;
CREATE TABLE `board` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    `name` VARCHAR(50) NOT NULL COMMENT '板块名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '板块描述',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '板块图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- 插入默认板块
INSERT INTO `board` (`id`, `name`, `description`, `sort`) VALUES
(1, '交流', '日常交流分享', 1),
(2, '学习交流', '学习资料和经验分享', 2),
(3, '兴趣搭子', '寻找志同道合的伙伴', 3),
(4, '二手交易', '闲置物品交易', 4),
(5, '校园活动', '校园活动信息发布', 5);

-- =====================================================
-- 4. 帖子表 (post)
-- =====================================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `board_id` BIGINT NOT NULL COMMENT '板块ID',
    `title` VARCHAR(100) NOT NULL COMMENT '帖子标题',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `images` JSON DEFAULT NULL COMMENT '图片JSON数组',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT DEFAULT 0 COMMENT '评论次数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0删除 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_board_id` (`board_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- =====================================================
-- 5. 评论表 (comment)
-- =====================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（回复）',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0删除 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =====================================================
-- 6. 帖子点赞表 (like)
-- =====================================================
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- =====================================================
-- 7. 帖子收藏表 (collect)
-- =====================================================
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- =====================================================
-- 8. 通知表 (notification)
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `type` TINYINT NOT NULL COMMENT '通知类型：1评论 2点赞 3收藏',
    `from_user_id` BIGINT NOT NULL COMMENT '触发者ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（帖子ID等）',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 9. 闲置物品表 (item)
-- =====================================================
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `type` TINYINT NOT NULL COMMENT '类型：1收购 2出售',
    `title` VARCHAR(100) NOT NULL COMMENT '物品标题',
    `description` TEXT DEFAULT NULL COMMENT '物品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `images` JSON DEFAULT NULL COMMENT '图片JSON数组',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `contact_count` INT DEFAULT 0 COMMENT '联系次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0删除 1正常 2已完成 3已下架',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置物品表';

-- =====================================================
-- 10. 闲置收藏表 (item_collect)
-- =====================================================
DROP TABLE IF EXISTS `item_collect`;
CREATE TABLE `item_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置收藏表';

-- =====================================================
-- 11. 会话表 (conversation)
-- =====================================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` BIGINT NOT NULL COMMENT '用户1ID',
    `user2_id` BIGINT NOT NULL COMMENT '用户2ID',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后消息ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user1_user2` (`user1_id`, `user2_id`),
    KEY `idx_user1_id` (`user1_id`),
    KEY `idx_user2_id` (`user2_id`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =====================================================
-- 12. 消息表 (message)
-- =====================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =====================================================
-- 初始化完成
-- =====================================================
SELECT '数据库初始化完成！' AS result;
