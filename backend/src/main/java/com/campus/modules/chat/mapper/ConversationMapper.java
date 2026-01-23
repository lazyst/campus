package com.campus.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
