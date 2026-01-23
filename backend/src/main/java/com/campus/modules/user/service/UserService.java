package com.campus.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);

    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 注册新用户
     */
    User register(String phone, String password, String nickname);

    /**
     * 更新用户信息
     */
    void updateProfile(Long userId, String nickname, Integer gender, String bio, String avatar);
}
