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
-- 密码: Test123456
-- BCrypt加密后的密码 (60字符)
-- =====================================================
INSERT INTO user (id, phone, password, nickname, avatar, gender, bio, status, created_at, deleted) VALUES
(1, '13800000001', '$2b$12$MU1C3Wvn9tbQGXL6orMWa.pj2BFV5sNAhuESRSq6mEO.dhvfu/X/i', 'User1', 'https://example.com/avatar1.jpg', 1, 'User1 Bio', 1, NOW(), 0),
(2, '13800000002', '$2b$12$MU1C3Wvn9tbQGXL6orMWa.pj2BFV5sNAhuESRSq6mEO.dhvfu/X/i', 'User2', 'https://example.com/avatar2.jpg', 2, 'User2 Bio', 1, NOW(), 0),
(3, '13800000003', '$2b$12$MU1C3Wvn9tbQGXL6orMWa.pj2BFV5sNAhuESRSq6mEO.dhvfu/X/i', 'User3', 'https://example.com/avatar3.jpg', 0, 'User3 Bio', 1, NOW(), 0);

-- =====================================================
-- 测试管理员数据
-- 密码: Admin123456
-- BCrypt加密后的密码 (60字符)
-- =====================================================
INSERT INTO admin (id, username, password, nickname, role, status, created_at, deleted) VALUES
(1, 'testadmin', '$2b$12$5OcjzeCdyFKAFuKnHcIM/ubOW92OlZi6HuDKZ9C9Gx66K060rLRBu', 'Admin', 1, 1, NOW(), 0);

-- =====================================================
-- 测试板块数据
-- =====================================================
INSERT INTO board (id, name, description, sort, status, created_at, deleted) VALUES
(1, 'Board1', 'Board 1 desc', 1, 1, NOW(), 0),
(2, 'Board2', 'Board 2 desc', 2, 1, NOW(), 0),
(3, 'Board3', 'Board 3 desc', 3, 1, NOW(), 0);

-- =====================================================
-- 测试帖子数据
-- =====================================================
INSERT INTO post (id, user_id, board_id, title, content, images, view_count, like_count, comment_count, collect_count, created_at, deleted) VALUES
(1, 1, 1, 'Post Title 1', 'Post content 1', '[]', 0, 0, 0, 0, NOW(), 0),
(2, 1, 1, 'Post Title 2', 'Post content 2', '[]', 0, 0, 0, 0, NOW(), 0),
(3, 2, 2, 'Post Title 3', 'Post content 3', '[]', 0, 0, 0, 0, NOW(), 0),
(4, 2, 2, 'Post Title 4', 'Post content 4', '[]', 0, 0, 0, 0, NOW(), 0),
(5, 3, 3, 'Post Title 5', 'Post content 5', '[]', 0, 0, 0, 0, NOW(), 0);

-- =====================================================
-- 测试评论数据
-- =====================================================
INSERT INTO comment (id, user_id, post_id, content, created_at, deleted) VALUES
(1, 2, 1, 'Comment 1', NOW(), 0),
(2, 3, 1, 'Comment 2', NOW(), 0),
(3, 1, 3, 'Comment 3', NOW(), 0);

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
INSERT INTO notification (id, user_id, type, from_user_id, target_id, content, is_read, created_at, deleted) VALUES
(1, 1, 1, 2, 1, 'User2 commented', 0, NOW(), 0),
(2, 1, 2, 2, 1, 'User2 liked', 0, NOW(), 0),
(3, 1, 3, 2, 2, 'User2 collected', 1, NOW(), 0);

-- =====================================================
-- 测试商品数据
-- =====================================================
INSERT INTO item (id, user_id, type, title, description, price, images, status, view_count, contact_count, created_at, deleted) VALUES
(1, 1, 2, 'Item 1', 'Item 1 desc', 99.99, '[]', 1, 0, 0, NOW(), 0),
(2, 2, 2, 'Item 2', 'Item 2 desc', 199.99, '[]', 1, 0, 0, NOW(), 0),
(3, 3, 1, 'Item 3', 'Item 3 desc', 299.99, '[]', 1, 0, 0, NOW(), 0);

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
(1, 1, 1, 2, 'Message 1', 1, NOW(), 0),
(2, 1, 2, 1, 'Message 2', 1, NOW(), 0),
(3, 2, 2, 3, 'Message 3', 1, NOW(), 0);
