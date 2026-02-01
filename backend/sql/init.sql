-- =====================================================
-- 校园平台数据库初始化脚本
-- Database: campus
-- Created for: Full Stack Campus Platform
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus;

-- =====================================================
-- 管理员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `admin` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（登录账号）',
    `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
    `role` tinyint DEFAULT '2' COMMENT '角色等级：1-超级管理员，2-普通管理员',
    `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =====================================================
-- 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号（登录账号）',
    `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
    `gender` tinyint DEFAULT '1' COMMENT '性别：1-男，2-女',
    `bio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人简介',
    `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
    `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
    `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级',
    `major` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 论坛板块表
-- =====================================================
CREATE TABLE IF NOT EXISTS `board` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '板块名称',
    `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '板块描述',
    `icon` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '板块图标',
    `sort` int DEFAULT '0' COMMENT '排序',
    `status` tinyint DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛板块表';

-- =====================================================
-- 帖子表
-- =====================================================
CREATE TABLE IF NOT EXISTS `post` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` bigint NOT NULL COMMENT '发布者ID',
    `board_id` bigint NOT NULL COMMENT '板块ID',
    `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
    `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子内容',
    `images` json DEFAULT NULL COMMENT '图片JSON数组',
    `view_count` int DEFAULT '0' COMMENT '浏览次数',
    `like_count` int DEFAULT '0' COMMENT '点赞次数',
    `comment_count` int DEFAULT '0' COMMENT '评论次数',
    `collect_count` int DEFAULT '0' COMMENT '收藏次数',
    `status` tinyint DEFAULT '1' COMMENT '状态：0删除 1正常',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_board_id` (`board_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- =====================================================
-- 评论表
-- =====================================================
CREATE TABLE IF NOT EXISTS `comment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` bigint NOT NULL COMMENT '评论者ID',
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（回复）',
    `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
    `status` tinyint DEFAULT '1' COMMENT '状态：0删除 1正常',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =====================================================
-- 帖子收藏表
-- =====================================================
CREATE TABLE IF NOT EXISTS `collect` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`,`post_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- =====================================================
-- 帖子点赞表
-- =====================================================
CREATE TABLE IF NOT EXISTS `like` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`,`post_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- =====================================================
-- 闲置物品表
-- =====================================================
CREATE TABLE IF NOT EXISTS `item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id` bigint NOT NULL COMMENT '发布者ID',
    `type` tinyint NOT NULL COMMENT '类型：1-求购 2-出售',
    `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品标题',
    `description` text COLLATE utf8mb4_unicode_ci COMMENT '物品描述',
    `price` decimal(10,2) NOT NULL COMMENT '价格',
    `images` json DEFAULT NULL COMMENT '图片JSON数组',
    `view_count` int DEFAULT '0' COMMENT '浏览次数',
    `contact_count` int DEFAULT '0' COMMENT '联系次数',
    `status` tinyint DEFAULT '1' COMMENT '状态：0删除 1正常 2已完成 3已下架',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0未删除 1已删除',
    `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交易地点',
    `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置物品表';

-- =====================================================
-- 物品收藏表
-- =====================================================
CREATE TABLE IF NOT EXISTS `item_collect` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `item_id` bigint NOT NULL COMMENT '物品ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`,`item_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品收藏表';

-- =====================================================
-- 消息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` bigint NOT NULL COMMENT '会话ID',
    `sender_id` bigint NOT NULL COMMENT '发送者ID',
    `receiver_id` bigint NOT NULL COMMENT '接收者ID',
    `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0=正常，1=已删除）',
    `type` int DEFAULT '1' COMMENT '消息类型: 1-文本, 2-图片, 3-语音',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =====================================================
-- 通知表
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` bigint NOT NULL COMMENT '接收者ID',
    `type` tinyint NOT NULL COMMENT '通知类型：1评论 2点赞 3收藏',
    `from_user_id` bigint NOT NULL COMMENT '触发者ID',
    `target_id` bigint NOT NULL COMMENT '目标ID（帖子ID等）',
    `comment_id` bigint DEFAULT NULL COMMENT '相关评论ID',
    `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知内容',
    `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0未读 1已读',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 会话表
-- =====================================================
CREATE TABLE IF NOT EXISTS `conversation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id1` bigint NOT NULL COMMENT '用户1ID',
    `user_id2` bigint NOT NULL COMMENT '用户2ID',
    `last_message_id` bigint DEFAULT NULL COMMENT '最后消息ID',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
    `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0=正常，1=已删除）',
    `unread_count1` int DEFAULT '0' COMMENT '用户1未读消息数',
    `unread_count2` int DEFAULT '0' COMMENT '用户2未读消息数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user1_user2` (`user_id1`,`user_id2`),
    KEY `idx_user1_id` (`user_id1`),
    KEY `idx_user2_id` (`user_id2`),
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =====================================================
-- 示例数据
-- =====================================================

-- 插入管理员数据
INSERT INTO `admin` (`id`, `username`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`, `last_login_ip`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`) VALUES
(1, 'admin', '$2a$10$YxE39kUPApogVkwimnlYOuW0xSM89h8nNda/Q0BOIHTKzKta8Tl/W', '超级管理员', NULL, 1, 1, '2026-02-01 16:06:12', NULL, '2026-01-27 18:48:18', '2026-01-27 18:48:18', NULL, NULL, 0);

-- 插入用户数据
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `gender`, `bio`, `avatar`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`, `grade`, `major`) VALUES
(2, '13900000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '校园小明', 1, '爱学习，喜欢交友的在校大学生', NULL, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0, NULL, NULL),
(3, '13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '校园小花', 2, '大四学生，分享校园生活', NULL, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0, NULL, NULL),
(4, '13900000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '学习达人', 1, '考研党，分享学习资料', NULL, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0, NULL, NULL),
(5, '13900000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '二手小王', 1, '专注二手交易，帮大家淘好物', NULL, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0, NULL, NULL),
(6, '13900000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '活动小精灵', 2, '校园活动组织者', NULL, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0, NULL, NULL);

-- 插入板块数据
INSERT INTO `board` (`id`, `name`, `description`, `icon`, `sort`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`) VALUES
(1, '交流', '日常交流分享', '', 1, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0),
(2, '学习交流', '学习资料和经验分享', NULL, 2, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0),
(3, '兴趣交友', '寻找志同道合的伙伴', NULL, 3, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0),
(4, '闲置交易', '闲置物品交易', NULL, 4, 1, '2026-01-27 18:45:40', '2026-02-01 03:15:53', NULL, NULL, 1),
(5, '校园活动', '校园活动信息发布', '', 4, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0);

-- 数据库初始化完成！
