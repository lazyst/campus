package com.campus.modules.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 板块创建请求DTO
 */
@Data
public class BoardCreateRequest {

    @NotBlank(message = "板块名称不能为空")
    @Size(max = 50, message = "板块名称不能超过50个字符")
    private String name;

    @Size(max = 255, message = "板块描述不能超过255个字符")
    private String description;

    private String icon;

    private Integer sort;
}
