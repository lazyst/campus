package com.campus.modules.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.forum.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
