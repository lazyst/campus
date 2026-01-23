package com.campus.modules.admin.controller;

import com.campus.common.Result;
import com.campus.modules.admin.entity.Admin;
import com.campus.modules.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                "avatar", admin.getAvatar(),
                "role", admin.getRole()
            ));
            
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
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
}
