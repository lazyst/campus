package com.campus.config;

import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.user.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试 Fixture 构建工具
 * 提供创建测试实体的静态方法
 */
public class FixtureBuilder {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setPhone("138" + String.format("%08d", id));
        user.setNickname("用户" + id);
        user.setPassword(PASSWORD_ENCODER.encode("password123"));
        user.setGender(1);
        user.setStatus(1);
        user.setBio("测试简介");
        user.setAvatar("https://example.com/avatar.jpg");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static User user(Long id, String phone, String nickname) {
        User user = user(id);
        user.setPhone(phone);
        user.setNickname(nickname);
        return user;
    }

    public static Post post(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(1L);
        post.setBoardId(1L);
        post.setTitle("测试帖子标题" + id);
        post.setContent("测试帖子内容" + id);
        post.setImages("[]");
        post.setViewCount(10);
        post.setLikeCount(5);
        post.setCommentCount(3);
        post.setCollectCount(2);
        post.setStatus(1);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    public static Post post(Long id, Long userId, Long boardId) {
        Post post = post(id);
        post.setUserId(userId);
        post.setBoardId(boardId);
        return post;
    }

    public static Comment comment(Long id) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUserId(1L);
        comment.setPostId(1L);
        comment.setParentId(null);
        comment.setContent("测试评论内容" + id);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    public static Comment comment(Long id, Long userId, Long postId, Long parentId) {
        Comment comment = comment(id);
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setParentId(parentId);
        return comment;
    }

    public static Board board(Long id) {
        Board board = new Board();
        board.setId(id);
        board.setName("测试板块" + id);
        board.setDescription("测试板块描述" + id);
        board.setIcon("icon" + id);
        board.setSort(id.intValue());
        board.setStatus(1);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        return board;
    }

    public static Item item(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(1L);
        item.setTitle("测试物品" + id);
        item.setDescription("测试物品描述" + id);
        item.setPrice(new BigDecimal("99.99"));
        item.setImages("[]");
        item.setCategory("电子产品");
        item.setType(Item.TYPE_SELL);
        item.setStatus(Item.STATUS_NORMAL);
        item.setViewCount(10);
        item.setContactCount(3);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    public static Item item(Long id, Long userId, Integer type) {
        Item item = item(id);
        item.setUserId(userId);
        item.setType(type);
        return item;
    }
}
