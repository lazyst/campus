package com.campus.modules.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子创建请求DTO
 */
@Data
public class PostCreateRequest {

    @NotNull(message = "板块ID不能为空")
    private Long boardId;

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题不能超过100个字符")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    private String content;

    private String images;
}
