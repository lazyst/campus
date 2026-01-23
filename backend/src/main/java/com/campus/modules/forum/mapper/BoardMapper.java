package com.campus.modules.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.forum.entity.Board;
import org.apache.ibatis.annotations.Mapper;

/**
 * 板块Mapper接口
 */
@Mapper
public interface BoardMapper extends BaseMapper<Board> {
}
