package com.campus.modules.user.dto;

import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateProfileRequest {

    private String nickname;

    private Integer gender;

    private String bio;

    private String avatar;

    private String grade;

    private String major;
}
