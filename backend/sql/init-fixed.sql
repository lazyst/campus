-- =====================================================
-- 补充字段 - 修复缺失的 updated_at, created_by, updated_by 字段
-- 执行时间: 2026-01-27
-- =====================================================

USE campus;

-- =====================================================
-- 8. 通知表 (notification) - 添加缺失字段
-- =====================================================
ALTER TABLE `notification`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`,
ADD COLUMN `comment_id` BIGINT DEFAULT NULL COMMENT '相关评论ID' AFTER `target_id`;

-- =====================================================
-- 帖子点赞表 (like) - 添加缺失字段
-- =====================================================
ALTER TABLE `like`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`;

-- =====================================================
-- 帖子收藏表 (collect) - 添加缺失字段
-- =====================================================
ALTER TABLE `collect`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`;

-- =====================================================
-- 闲置收藏表 (item_collect) - 添加缺失字段
-- =====================================================
ALTER TABLE `item_collect`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`;

-- =====================================================
-- 消息表 (message) - 添加缺失字段
-- =====================================================
ALTER TABLE `message`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`;

-- =====================================================
-- 会话表 (conversation) - 添加缺失字段
-- =====================================================
ALTER TABLE `conversation`
ADD COLUMN `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `updated_at`,
ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '更新者ID' AFTER `created_by`;

-- =====================================================
-- 验证修复
-- =====================================================
SELECT
  TABLE_NAME,
  GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as columns
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA='campus'
  AND TABLE_NAME IN ('notification', '`like`', '`collect`', 'item_collect', 'message', 'conversation')
GROUP BY TABLE_NAME;
