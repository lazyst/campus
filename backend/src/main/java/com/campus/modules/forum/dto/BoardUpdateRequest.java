package com.campus.modules.forum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 板块更新请求DTO
 */
@Data
public class BoardUpdateRequest {

    @Size(max = 50, message = "板块名称不能超过50个字符")
    private String name;

    @Size(max = 255, message = "板块描述不能超过255个字符")
    private String description;

    private String icon;

    private Integer sort;

    private Integer status;
}
