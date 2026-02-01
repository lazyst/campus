package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.Result;
import com.campus.modules.admin.entity.Admin;
import com.campus.modules.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@Tag(name = "管理员认证")
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            String token = adminService.login(request.getUsername(), request.getPassword());

            Admin admin = adminService.getByUsername(request.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "nickname", admin.getNickname(),
                "avatar", admin.getAvatar() != null ? admin.getAvatar() : "",
                "role", admin.getRole()
            ));

            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "初始化超级管理员（开发环境）")
    @PostMapping("/init-admin")
    public Result<Map<String, Object>> initAdmin(@RequestBody InitRequest request) {
        // 检查是否已存在超级管理员
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getRole, 1);
        Admin existingSuperAdmin = adminService.getOne(wrapper);

        if (existingSuperAdmin != null) {
            return Result.error("超级管理员已存在");
        }

        // 创建超级管理员
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setNickname(request.getNickname() != null ? request.getNickname() : "超级管理员");
        admin.setRole(1); // 1 = 超级管理员
        admin.setStatus(1);

        adminService.save(admin);

        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("role", admin.getRole());

        return Result.success(result);
    }

    @Operation(summary = "重置超级管理员密码（开发环境）")
    @PostMapping("/reset-admin-password")
    public Result<Void> resetAdminPassword(@RequestBody InitRequest request) {
        // 检查是否已存在超级管理员
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getRole, 1);
        Admin existingSuperAdmin = adminService.getOne(wrapper);

        if (existingSuperAdmin == null) {
            return Result.error("超级管理员不存在，请先调用 init-admin 创建");
        }

        // 更新密码
        existingSuperAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getNickname() != null) {
            existingSuperAdmin.setNickname(request.getNickname());
        }
        adminService.updateById(existingSuperAdmin);

        return Result.success();
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 初始化管理员请求DTO
     */
    public static class InitRequest {
        private String username;
        private String password;
        private String nickname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
