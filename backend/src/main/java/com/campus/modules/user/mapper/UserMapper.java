package com.campus.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除用户（忽略逻辑删除）
     */
    @Update("DELETE FROM user WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    /**
     * 根据手机号查询用户（忽略逻辑删除）
     */
    @Select("SELECT * FROM user WHERE phone = #{phone} LIMIT 1")
    User selectByPhoneIncludingDeleted(@Param("phone") String phone);

    /**
     * 物理更新用户deleted状态（忽略逻辑删除）
     */
    @Update("UPDATE user SET deleted = #{deleted} WHERE id = #{id}")
    int updateDeletedStatus(@Param("id") Long id, @Param("deleted") Integer deleted);
}
