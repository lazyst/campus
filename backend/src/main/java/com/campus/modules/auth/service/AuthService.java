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
     * 从请求头获取用户ID
     * @param authHeader Authorization请求头的值
     * @return 用户ID；如果无效返回null
     */
    Long getUserIdFromAuthHeader(String authHeader);

    /**
     * 刷新令牌
     */
    String refreshToken(String token);

    /**
     * 检查手机号是否已注册（被删除的也算已注册）
     */
    boolean isPhoneRegistered(String phone);

    /**
     * 获取被删除的用户信息（如果存在）
     */
    com.campus.modules.user.entity.User getDeletedUserByPhone(String phone);
}
