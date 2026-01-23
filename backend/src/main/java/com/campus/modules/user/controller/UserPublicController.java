package com.campus.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户公开信息控制器
 * 提供无需认证的用户信息查询API
 */
@Tag(name = "用户公开信息")
@RestController
@RequestMapping("/api/users")
public class UserPublicController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final ItemMapper itemMapper;

    public UserPublicController(UserMapper userMapper, PostMapper postMapper, ItemMapper itemMapper) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.itemMapper = itemMapper;
    }

    @Operation(summary = "获取用户公开信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("createdAt", user.getCreatedAt());

        return Result.success(profile);
    }

    @Operation(summary = "获取用户的帖子列表")
    @GetMapping("/{id}/posts")
    public Result<Page<Post>> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // Verify user exists
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, id)
               .eq(Post::getStatus, 1)  // Only normal status posts
               .orderByDesc(Post::getCreatedAt);
        
        postMapper.selectPage(postPage, wrapper);

        return Result.success(postPage);
    }

    @Operation(summary = "获取用户的物品列表")
    @GetMapping("/{id}/items")
    public Result<Page<Item>> getUserItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // Verify user exists
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Page<Item> itemPage = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getUserId, id)
               .eq(Item::getStatus, 1)  // Only normal status items
               .orderByDesc(Item::getCreatedAt);
        
        itemMapper.selectPage(itemPage, wrapper);

        return Result.success(itemPage);
    }
}
