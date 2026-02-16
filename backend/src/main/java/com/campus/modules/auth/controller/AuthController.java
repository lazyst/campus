package com.campus.modules.auth.controller;

import com.campus.common.Result;
import com.campus.modules.auth.dto.LoginRequest;
import com.campus.modules.auth.dto.RegisterRequest;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getPhone(), request.getPassword(), request.getNickname());
        String token = authService.generateToken(user);
        return Result.success(user).setToken(token);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getPhone(), request.getPassword());
        return Result.success(token).setToken(token);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("无效的令牌");
        }
        String token = authorization.substring(7);
        String newToken = authService.refreshToken(token);
        if (newToken != null) {
            return Result.success(newToken).setToken(newToken);
        }
        return Result.error("令牌已过期，请重新登录");
    }

    @Operation(summary = "检查手机号是否已注册")
    @GetMapping("/check-phone")
    public Result<PhoneCheckResult> checkPhoneRegistered(@RequestParam String phone) {
        boolean registered = authService.isPhoneRegistered(phone);
        User deletedUser = authService.getDeletedUserByPhone(phone);

        PhoneCheckResult result = new PhoneCheckResult();
        result.setRegistered(registered);
        result.setWasDeleted(deletedUser != null);
        if (deletedUser != null) {
            result.setPreviousNickname(deletedUser.getNickname());
        }
        return Result.success(result);
    }

    /**
     * 手机号检查结果
     */
    public static class PhoneCheckResult {
        private boolean registered;
        private boolean wasDeleted;
        private String previousNickname;

        public boolean isRegistered() { return registered; }
        public void setRegistered(boolean registered) { this.registered = registered; }
        public boolean isWasDeleted() { return wasDeleted; }
        public void setWasDeleted(boolean wasDeleted) { this.wasDeleted = wasDeleted; }
        public String getPreviousNickname() { return previousNickname; }
        public void setPreviousNickname(String previousNickname) { this.previousNickname = previousNickname; }
    }
}
