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
            throw new IllegalArgumentException("用户名或密码错误");
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
            User user = userService.getByPhone(phone);

            if (user == null) {
                return null;
            }

            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String refreshToken(String token) {
        try {
            // 验证 token 是否有效
            if (!jwtConfig.validateToken(token, null)) {
                return null;
            }
            // 刷新 token
            return jwtConfig.refreshToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isPhoneRegistered(String phone) {
        // 查询包括已删除的用户
        User deletedUser = userService.getByPhoneIncludingDeleted(phone);
        return deletedUser != null;
    }

    @Override
    public User getDeletedUserByPhone(String phone) {
        User user = userService.getByPhoneIncludingDeleted(phone);
        if (user != null && user.getDeleted() != null && user.getDeleted() == 1) {
            return user;
        }
        return null;
    }
}
