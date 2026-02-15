package com.campus.modules.auth.service;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    String login(String phone, String password);

    /**
     * 用户注册
     */
    com.campus.modules.user.entity.User register(String phone, String password, String nickname);

    /**
     * 生成JWT令牌
     */
    String generateToken(com.campus.modules.user.entity.User user);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 验证令牌
     */
    boolean validateToken(String token);

    /**
     * 从令牌获取用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 刷新令牌
     */
    String refreshToken(String token);
}
