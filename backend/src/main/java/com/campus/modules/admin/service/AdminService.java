package com.campus.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.admin.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService extends IService<Admin> {

    /**
     * 根据用户名获取管理员
     */
    Admin getByUsername(String username);

    /**
     * 管理员登录
     */
    String login(String username, String password);

    /**
     * 检查是否为超级管理员
     */
    boolean isSuperAdmin(Long adminId);

    /**
     * 从token中获取管理员ID
     */
    Long getAdminIdFromToken(String token);
}
