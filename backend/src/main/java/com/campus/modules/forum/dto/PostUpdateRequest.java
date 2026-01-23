package com.campus.modules.forum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子更新请求DTO
 */
@Data
public class PostUpdateRequest {

    @Size(max = 100, message = "帖子标题不能超过100个字符")
    private String title;

    private String content;

    private String images;

    private Integer status;
}
