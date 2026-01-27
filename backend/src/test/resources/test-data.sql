-- 校园互助平台测试数据脚本
-- 在每次测试前执行，清空表并插入测试数据
-- Database: campus_test

-- 清空所有表（按依赖顺序）
DELETE FROM message;
DELETE FROM conversation;
DELETE FROM notification;
DELETE FROM item_collect;
DELETE FROM item;
DELETE FROM collect;
DELETE FROM like_record;
DELETE FROM comment;
DELETE FROM post;
DELETE FROM board;
DELETE FROM admin;
DELETE FROM user WHERE phone LIKE 'test%';

-- 重置自增ID
ALTER TABLE user AUTO_INCREMENT = 1;
ALTER TABLE admin AUTO_INCREMENT = 1;
ALTER TABLE board AUTO_INCREMENT = 1;
ALTER TABLE post AUTO_INCREMENT = 1;
ALTER TABLE comment AUTO_INCREMENT = 1;
ALTER TABLE like_record AUTO_INCREMENT = 1;
ALTER TABLE collect AUTO_INCREMENT = 1;
ALTER TABLE notification AUTO_INCREMENT = 1;
ALTER TABLE item AUTO_INCREMENT = 1;
ALTER TABLE item_collect AUTO_INCREMENT = 1;
ALTER TABLE conversation AUTO_INCREMENT = 1;
ALTER TABLE message AUTO_INCREMENT = 1;

-- =====================================================
-- 测试用户数据
-- 密码统一为: Test123456
-- BCrypt加密后的密码
-- =====================================================
INSERT INTO user (id, phone, password, nickname, avatar, gender, bio, status, created_at, deleted) VALUES
(1, 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EH', '测试用户1', 'https://example.com/avatar1.jpg', 1, '测试用户1', 1, NOW(), 0),
(2, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EH', '测试用户2', 'https://example.com/avatar2.jpg', 2, '测试用户2', 1, NOW(), 0),
(3, 'testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EH', '测试用户3', 'https://example.com/avatar3.jpg', 0, '测试用户3', 1, NOW(), 0);

-- =====================================================
-- 测试管理员数据
-- 密码统一为: Admin123456
-- =====================================================
INSERT INTO admin (id, username, password, nickname, role, status, created_at, deleted) VALUES
(1, 'testadmin', '$2a$10$N.zmdr9k7uOCQb376NoUn', '测试管理员', 1, 1, NOW(), 0);

-- =====================================================
-- 测试板块数据
-- =====================================================
INSERT INTO board (id, name, description, sort, status, created_at, deleted) VALUES
(1, '测试板块1', '这是测试板块1的描述', 1, 1, NOW(), 0),
(2, '测试板块2', '这是测试板块2的描述', 2, 1, NOW(), 0),
(3, '测试板块3', '这是测试板块3的描述', 3, 1, NOW(), 0);

-- =====================================================
-- 测试帖子数据
-- =====================================================
INSERT INTO post (id, user_id, board_id, title, content, images, view_count, like_count, comment_count, collect_count, created_at, deleted) VALUES
(1, 1, 1, '测试帖子1标题', '这是测试帖子1的内容', '["https://example.com/image1.jpg"]', 0, 0, 0, 0, NOW(), 0),
(2, 1, 1, '测试帖子2标题', '这是测试帖子2的内容', '[]', 0, 0, 0, 0, NOW(), 0),
(3, 2, 2, '测试帖子3标题', '这是测试帖子3的内容', '[]', 0, 0, 0, 0, NOW(), 0),
(4, 2, 2, '测试帖子4标题', '这是测试帖子4的内容', '[]', 0, 0, 0, 0, NOW(), 0),
(5, 3, 3, '测试帖子5标题', '这是测试帖子5的内容', '[]', 0, 0, 0, 0, NOW(), 0);

-- =====================================================
-- 测试评论数据
-- =====================================================
INSERT INTO comment (id, user_id, post_id, content, created_at, deleted) VALUES
(1, 2, 1, '这是测试评论1', NOW(), 0),
(2, 3, 1, '这是测试评论2', NOW(), 0),
(3, 1, 3, '这是测试评论3', NOW(), 0);

-- =====================================================
-- 测试点赞数据
-- =====================================================
INSERT INTO like_record (id, user_id, post_id, created_at, deleted) VALUES
(1, 2, 1, NOW(), 0),
(2, 3, 1, NOW(), 0),
(3, 1, 3, NOW(), 0);

-- =====================================================
-- 测试收藏数据
-- =====================================================
INSERT INTO collect (id, user_id, post_id, created_at, deleted) VALUES
(1, 2, 2, NOW(), 0),
(2, 3, 2, NOW(), 0),
(3, 1, 4, NOW(), 0);

-- =====================================================
-- 测试通知数据
-- =====================================================
INSERT INTO notification (id, user_id, type, content, target_id, is_read, created_at, deleted) VALUES
(1, 1, 1, '用户2评论了你的帖子', 1, 0, NOW(), 0),
(2, 1, 2, '用户2点赞了你的帖子', 1, 0, NOW(), 0),
(3, 1, 3, '用户2收藏了你的帖子', 2, 1, NOW(), 0);

-- =====================================================
-- 测试商品数据
-- =====================================================
INSERT INTO item (id, user_id, type, title, description, price, images, status, view_count, contact_count, created_at, deleted) VALUES
(1, 1, 2, '测试商品1', '这是测试商品1的描述', 99.99, '["https://example.com/item1.jpg"]', 1, 0, 0, NOW(), 0),
(2, 2, 2, '测试商品2', '这是测试商品2的描述', 199.99, '[]', 1, 0, 0, NOW(), 0),
(3, 3, 1, '测试商品3', '这是测试商品3的描述', 299.99, '[]', 1, 0, 0, NOW(), 0);

-- =====================================================
-- 测试商品收藏数据
-- =====================================================
INSERT INTO item_collect (id, user_id, item_id, created_at, deleted) VALUES
(1, 2, 1, NOW(), 0),
(2, 3, 1, NOW(), 0),
(3, 1, 2, NOW(), 0);

-- =====================================================
-- 测试会话数据
-- =====================================================
INSERT INTO conversation (id, user_id_1, user_id_2, last_message_id, created_at, deleted) VALUES
(1, 1, 2, NULL, NOW(), 0),
(2, 2, 3, NULL, NOW(), 0);

-- =====================================================
-- 测试消息数据
-- =====================================================
INSERT INTO message (id, conversation_id, sender_id, receiver_id, content, type, created_at, deleted) VALUES
(1, 1, 1, 2, '这是测试消息1', 1, NOW(), 0),
(2, 1, 2, 1, '这是测试消息2', 1, NOW(), 0),
(3, 2, 2, 3, '这是测试消息3', 1, NOW(), 0);
