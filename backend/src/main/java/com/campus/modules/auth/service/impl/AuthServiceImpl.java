package com.campus.modules.auth.service.impl;

import com.campus.config.JwtConfig;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtConfig jwtConfig;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserService userService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String login(String phone, String password) {
        User user = userService.getByPhone(phone);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        return jwtConfig.generateToken(user.getPhone());
    }

    @Override
    public User register(String phone, String password, String nickname) {
        return userService.register(phone, password, nickname);
    }

    @Override
    public String generateToken(User user) {
        return jwtConfig.generateToken(user.getPhone());
    }

    @Override
    public void logout() {
        // JWT是无状态的，登出只需要客户端删除令牌
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String phone = jwtConfig.getUsernameFromToken(token);
            User user = userService.getByPhone(phone);
            return user != null && user.getStatus() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            String phone = jwtConfig.getUsernameFromToken(token);
            System.out.println("=== AuthServiceImpl.getUserIdFromToken ===");
            System.out.println("token: " + token.substring(0, Math.min(30, token.length())) + "...");
            System.out.println("解析出的phone: " + phone);

            User user = userService.getByPhone(phone);
            System.out.println("查询到的user: " + (user == null ? "null" : "id=" + user.getId() + ", status=" + user.getStatus()));

            if (user == null) {
                System.out.println("用户不存在，返回null");
                return null;
            }

            System.out.println("返回userId: " + user.getId());
            return user.getId();
        } catch (Exception e) {
            // Token无效或解析失败
            System.out.println("解析token异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
