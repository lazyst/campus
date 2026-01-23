package com.campus.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {

    /**
     * 手机号（唯一）
     */
    private String phone;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：0未知 1男 2女
     */
    private Integer gender;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 状态：0禁用 1正常
     */
    private Integer status;
}
