package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("board")
public class Board extends BaseEntity {
    private String name;
    private String description;
    private String icon;
    private Integer sort;
    private Integer status;
}
