package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 简化版收藏实体类（测试用）
 */
@Getter
@Setter
@TableName("`collect`")
public class CollectSimple {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;
}
