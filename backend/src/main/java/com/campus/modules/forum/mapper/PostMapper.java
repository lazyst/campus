package com.campus.modules.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子Mapper接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
