package com.campus.modules.user.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.dto.UpdateProfileRequest;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 未登录用户返回空数据
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.success(null);
        }
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = authService.getUserIdFromToken(token);
            
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
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        System.out.println("=== DEBUG === userId: " + userId);
        System.out.println("=== DEBUG === request: " + request);
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
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

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
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path uploadDir = Paths.get("./uploads/avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String ext = ".jpg";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL
            String avatarUrl = "/uploads/avatars/" + filename;

            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}
