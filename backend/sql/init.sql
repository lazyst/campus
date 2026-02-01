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
(4, '校园活动', '校园活动信息发布', '', 4, 1, '2026-01-27 18:45:40', '2026-01-27 18:45:40', NULL, NULL, 0);

-- 插入帖子数据
INSERT INTO `post` (`id`, `user_id`, `board_id`, `title`, `content`, `images`, `view_count`, `like_count`, `comment_count`, `collect_count`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`) VALUES
(1, 2, 1, '新生入学必备物品清单', '马上要开学了，给大家分享一下需要带的东西：\n1. 床上用品（被子、枕头、床单）\n2. 洗漱用品\n3. 学习用品（笔记本、笔、本子）\n4. 换洗衣物\n5. 电子产品（手机、电脑、充电器）\n大家还有什么要补充的吗？', NULL, 156, 12, 5, 3, 1, '2026-01-28 10:00:00', '2026-01-28 10:00:00', NULL, NULL, 0),
(2, 3, 2, '出售高等数学教材', '本人大三，高数教材第七版，保存完好，无笔记。需要的同学联系，价格可议。', NULL, 89, 5, 2, 1, 1, '2026-01-28 14:30:00', '2026-01-28 14:30:00', NULL, NULL, 0),
(3, 4, 2, '考研资料全套转让', '考研用过的全套资料，包括政治、英语、数学历年真题和模拟题，几乎没怎么用过，半价出。', NULL, 234, 18, 8, 5, 1, '2026-01-29 09:15:00', '2026-01-29 09:15:00', NULL, NULL, 0),
(4, 6, 4, '周末篮球友谊赛组队', '本周六下午3点，学院篮球场组织友谊赛，缺2人组队，有兴趣的同学报名参加！', NULL, 67, 8, 12, 2, 1, '2026-01-29 16:00:00', '2026-01-29 16:00:00', NULL, NULL, 0),
(5, 2, 3, '找一起学英语的伙伴', '本人英语四级还没过，想找小伙伴一起学习，互相监督。有一起的吗？', NULL, 45, 3, 7, 0, 1, '2026-01-30 08:00:00', '2026-01-30 08:00:00', NULL, NULL, 0),
(6, 5, 1, '食堂哪个窗口最好吃？', '快来推荐一下学校食堂好吃的窗口吧！我先来，三楼的麻辣香锅YYDS！', NULL, 312, 25, 45, 8, 1, '2026-01-30 12:00:00', '2026-01-30 12:00:00', NULL, NULL, 0),
(7, 3, 2, '出闲置打印机一台', '惠普家用打印机，支持打印、复印、扫描，功能完好，墨盒还有半罐。需要的同学联系。', NULL, 178, 9, 3, 2, 1, '2026-01-30 18:30:00', '2026-01-30 18:30:00', NULL, NULL, 0),
(8, 4, 2, '求购二手自行车', '预算100-200，能代步就行，质量好一点，有出的同学请联系。', NULL, 34, 2, 1, 0, 1, '2026-01-31 10:00:00', '2026-01-31 10:00:00', NULL, NULL, 0),
(9, 6, 3, '吉他新手求组队', '刚学吉他，想找几个一起学的朋友互相交流进步，有一起的吗？', NULL, 56, 6, 9, 1, 1, '2026-01-31 14:00:00', '2026-01-31 14:00:00', NULL, NULL, 0),
(10, 2, 4, '毕业季跳蚤市场预告', '下周日学校操场将举办毕业季跳蚤市场，欢迎大家来淘好物！', NULL, 189, 15, 6, 4, 1, '2026-02-01 09:00:00', '2026-02-01 09:00:00', NULL, NULL, 0);

-- 插入闲置物品数据
INSERT INTO `item` (`id`, `user_id`, `type`, `title`, `description`, `price`, `images`, `view_count`, `contact_count`, `status`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`, `location`, `category`) VALUES
(1, 2, 2, '一加平板', '一加平板，95新，平时用来记笔记看视频，无划痕，配件齐全。', 2300.00, NULL, 45, 3, 1, '2026-01-28 15:00:00', '2026-01-28 15:00:00', NULL, NULL, 0, '南校区图书馆', '电子产品'),
(2, 3, 2, '出高等数学教材', '高等数学教材第七版，考研用完保存完好，无笔记。适合大一新生。', 25.00, NULL, 67, 5, 1, '2026-01-28 16:00:00', '2026-01-28 16:00:00', NULL, NULL, 0, '北校区食堂', '书籍教材'),
(3, 4, 2, '闲置打印机', '惠普家用打印机，支持打印、复印、扫描，功能完好。墨盒还有半罐。', 150.00, NULL, 89, 7, 1, '2026-01-29 10:00:00', '2026-01-29 10:00:00', NULL, NULL, 0, '东校区宿舍', '办公设备'),
(4, 5, 2, '考研资料全套', '考研资料全套，包含政治、英语、数学历年真题和模拟题，半价出。', 80.00, NULL, 123, 8, 1, '2026-01-29 11:00:00', '2026-01-29 11:00:00', NULL, NULL, 0, '图书馆一楼', '书籍教材'),
(5, 2, 2, '宿舍小冰箱', '30L容量小冰箱，制冷效果很好，宿舍夏天必备。毕业了带不走。', 200.00, NULL, 156, 12, 1, '2026-01-30 09:00:00', '2026-01-30 09:00:00', NULL, NULL, 0, '西校区宿舍', '生活电器'),
(6, 3, 1, '收一辆自行车', '预算100-200，能代步就行，质量好一点，最好有后座。', 150.00, NULL, 34, 2, 1, '2026-01-30 14:00:00', '2026-01-30 14:00:00', NULL, NULL, 0, '全校均可', '交通工具'),
(7, 4, 1, '收二手吉他', '初学者入门吉他，要求无明显损坏，有调音器赠送更好。', 300.00, NULL, 28, 1, 1, '2026-01-31 10:00:00', '2026-01-31 10:00:00', NULL, NULL, 0, '南校区', '乐器'),
(8, 5, 1, '收计算器', '考研用，需要科学计算器卡西欧991，有出的联系我。', 80.00, NULL, 19, 1, 1, '2026-01-31 16:00:00', '2026-01-31 16:00:00', NULL, NULL, 0, '图书馆', '学习用品'),
(9, 6, 2, '全新未拆封台灯', '买错了型号，出一个护眼台灯，LED光源，三档可调，全新未拆封。', 45.00, NULL, 78, 4, 1, '2026-02-01 08:00:00', '2026-02-01 08:00:00', NULL, NULL, 0, '北校区', '生活电器'),
(10, 2, 2, '二手电风扇', '夏天快到了，出一个台式电风扇，三档可调，制冷效果好。', 35.00, NULL, 42, 2, 1, '2026-02-01 10:00:00', '2026-02-01 10:00:00', NULL, NULL, 0, '东校区', '生活电器'),
(11, 3, 2, '出瑜伽垫', '买来只用了两次，厚度8mm，附送收纳袋。', 40.00, NULL, 35, 1, 1, '2026-02-01 11:00:00', '2026-02-01 11:00:00', NULL, NULL, 0, '体育馆', '运动健身'),
(12, 4, 2, '键盘鼠标套装', '无线键盘鼠标套装，用了半年，功能正常，打游戏办公都可以。', 60.00, NULL, 52, 3, 1, '2026-02-01 14:00:00', '2026-02-01 14:00:00', NULL, NULL, 0, '实验室', '电子产品');

-- 数据库初始化完成！
