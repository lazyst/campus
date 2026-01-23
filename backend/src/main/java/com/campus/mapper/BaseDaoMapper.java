package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.BaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基础Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供CRUD功能
 *
 * @param <T> 实体类型，必须继承BaseEntity
 */
@Mapper
public interface BaseDaoMapper<T extends BaseEntity> extends BaseMapper<T> {
}
