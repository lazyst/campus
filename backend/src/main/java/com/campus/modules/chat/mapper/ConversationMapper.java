package com.campus.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 统计用户的所有未读消息数量
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN user_id1 = #{userId} THEN unread_count1 ELSE unread_count2 END), 0) " +
            "FROM conversation WHERE deleted = false AND (user_id1 = #{userId} OR user_id2 = #{userId})")
    Integer sumUnreadCount(@Param("userId") Long userId);
}
