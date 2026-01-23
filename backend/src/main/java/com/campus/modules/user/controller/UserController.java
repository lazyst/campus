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
    public Result<User> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        userService.updateProfile(userId, request.getNickname(), request.getGender(), request.getBio(), request.getAvatar());
        return Result.success();
    }
}
