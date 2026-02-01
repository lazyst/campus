package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.dto.UpdateProfileRequest;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台用户管理控制器
 */
@Tag(name = "后台用户管理")
@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserService userService;
    private final AdminService adminService;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManagementController(UserService userService, AdminService adminService, AuthService authService) {
        this.userService = userService;
        this.adminService = adminService;
        this.authService = authService;
    }

    @Operation(summary = "获取用户列表")
    @GetMapping
    public Result<Page<User>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        // Verify admin permission
        verifyAdmin(authHeader);

        Page<User> pageParam = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(User::getDeleted, false);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getPhone, keyword)
                            .or()
                            .like(User::getNickname, keyword));
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = userService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}")
    public Result<User> detail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {

        verifyAdmin(authHeader);

        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }

        // Clear sensitive data
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{userId}")
    public Result<User> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        verifyAdmin(authHeader);

        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }

        // 更新用户信息
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }

        userService.updateById(user);
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request) {
        
        verifyAdmin(authHeader);
        
        Integer status = request.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值无效");
        }
        
        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "封禁用户")
    @PutMapping("/{userId}/ban")
    public Result<Void> ban(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        
        verifyAdmin(authHeader);
        
        return updateStatus(authHeader, userId, Map.of("status", 0));
    }

    @Operation(summary = "解封用户")
    @PutMapping("/{userId}/unban")
    public Result<Void> unban(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        
        verifyAdmin(authHeader);
        
        return updateStatus(authHeader, userId, Map.of("status", 1));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        
        verifyAdmin(authHeader);
        
        User user = userService.getById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.error("用户不存在");
        }
        
        // Soft delete
        user.setDeleted(1);
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "获取用户统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader("Authorization") String authHeader) {
        
        verifyAdmin(authHeader);
        
        long total = userService.count();
        long normal = userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getDeleted, false)
                .eq(User::getStatus, 1));
        long banned = userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getDeleted, false)
                .eq(User::getStatus, 0));
        
        Map<String, Object> result = Map.of(
            "total", total,
            "normal", normal,
            "banned", banned
        );
        
        return Result.success(result);
    }

    /**
     * 验证管理员权限
     */
    private void verifyAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("未授权访问");
        }

        String token = authHeader.replace("Bearer ", "");
        Long adminId = adminService.getAdminIdFromToken(token);

        if (adminId == null || !adminService.isSuperAdmin(adminId)) {
            throw new SecurityException("权限不足");
        }
    }
}
