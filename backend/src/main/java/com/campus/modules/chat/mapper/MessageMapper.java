package com.campus.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 批量查询每个会话的最新消息
     */
    @Select("<script>" +
            "SELECT m.* FROM message m " +
            "INNER JOIN (" +
            "    SELECT conversation_id, MAX(created_at) as max_created " +
            "    FROM message " +
            "    WHERE deleted = false AND conversation_id IN " +
            "    <foreach collection='conversationIds' item='id' open='(' separator=',' close=')'>" +
            "        #{id}" +
            "    </foreach>" +
            "    GROUP BY conversation_id" +
            ") latest ON m.conversation_id = latest.conversation_id AND m.created_at = latest.max_created " +
            "WHERE m.deleted = false" +
            "</script>")
    List<Message> selectLastMessagesByConversationIds(@Param("conversationIds") List<Long> conversationIds);
}
