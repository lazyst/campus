package com.campus.modules.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.common.service.ImgBedService;
import com.campus.modules.user.dto.UpdateProfileRequest;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.mapper.UserMapper;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final ItemMapper itemMapper;
    private final ImgBedService imgBedService;

    public UserController(UserService userService, AuthService authService,
                         UserMapper userMapper, PostMapper postMapper, ItemMapper itemMapper,
                         ImgBedService imgBedService) {
        this.userService = userService;
        this.authService = authService;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.itemMapper = itemMapper;
        this.imgBedService = imgBedService;
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 未登录用户返回空数据
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.success(null);
        }
        
        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);
            
            // userId为null说明token无效
            if (userId == null) {
                return Result.success(null);
            }
            
            User user = userService.getById(userId);
            if (user == null) {
                return Result.success(null);
            }
            return Result.success(user);
        } catch (Exception e) {
            // Token无效时返回空数据
            return Result.success(null);
        }
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            return Result.error("无效的认证令牌");
        }
        userService.updateProfile(userId, request.getNickname(), request.getGender(), request.getBio(), request.getAvatar(), request.getGrade(), request.getMajor());
        return Result.success();
    }

    @Operation(summary = "获取用户公开信息")
    @GetMapping("/public/{userId}")
    public Result<User> getUserPublicInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 只返回公开信息（包含bio和gender）
        User publicUser = new User();
        publicUser.setId(user.getId());
        publicUser.setNickname(user.getNickname());
        publicUser.setAvatar(user.getAvatar());
        publicUser.setGender(user.getGender());
        publicUser.setBio(user.getBio());
        return Result.success(publicUser);
    }

    @Operation(summary = "获取用户详细信息（包含个人简介）")
    @GetMapping("/profile/{userId}")
    public Result<User> getUserDetailInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 返回完整用户信息（敏感信息除外）
        User detailUser = new User();
        detailUser.setId(user.getId());
        detailUser.setNickname(user.getNickname());
        detailUser.setAvatar(user.getAvatar());
        detailUser.setGender(user.getGender());
        detailUser.setBio(user.getBio());
        detailUser.setGrade(user.getGrade());
        detailUser.setMajor(user.getMajor());
        detailUser.setCreatedAt(user.getCreatedAt());
        return Result.success(detailUser);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        if (userId == null) {
            return Result.error("无效的认证令牌");
        }

        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 验证文件大小 (2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("文件大小不能超过2MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String ext = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;

            // 上传到图床
            String avatarUrl = imgBedService.uploadImage(file.getInputStream(), filename);

            if (avatarUrl == null) {
                return Result.error("头像上传失败，请稍后重试");
            }

            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "注销账号")
    @DeleteMapping("/account")
    public Result<Void> deactivateAccount(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        if (userId == null) {
            return Result.error("无效的认证令牌");
        }

        userService.deactivateAccount(userId);
        return Result.success();
    }

    // ========== 公开用户信息接口（原 UsersController 功能）==========

    @Operation(summary = "获取用户公开信息（兼容旧路径）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("gender", user.getGender());
        profile.put("createdAt", user.getCreatedAt());

        return Result.success(profile);
    }

    @Operation(summary = "获取用户的帖子列表")
    @GetMapping("/{id}/posts")
    public Result<Page<Post>> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, id)
               .eq(Post::getStatus, 1)
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

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Page<Item> itemPage = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getUserId, id)
               .eq(Item::getDeleted, false)
               .ne(Item::getStatus, 2)
               .ne(Item::getStatus, 3)
               .orderByDesc(Item::getCreatedAt);

        itemMapper.selectPage(itemPage, wrapper);

        return Result.success(itemPage);
    }
}
