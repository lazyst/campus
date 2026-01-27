package com.campus.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

/**
 * 测试工具类
 * 提供测试所需的公共方法
 */
public class TestUtils {

    /**
     * 为ServiceImpl注入baseMapper
     * 解决Mockito无法自动注入继承字段的问题
     *
     * @param service ServiceImpl实例
     * @param baseMapper Mock的BaseMapper实例
     * @param <M> Mapper类型
     * @param <T> 实体类型
     */
    public static <M extends BaseMapper<T>, T> void setBaseMapper(
            ServiceImpl<M, T> service,
            M baseMapper) {

        try {
            // 方法1: 使用ReflectionTestUtils（推荐）
            ReflectionTestUtils.setField(service, "baseMapper", baseMapper);

        } catch (Exception e) {
            // 方法2: 使用反射直接设置
            try {
                Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
                baseMapperField.setAccessible(true);
                baseMapperField.set(service, baseMapper);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException("Failed to inject baseMapper: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * 设置对象的私有字段值
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    public static void setField(Object target, String fieldName, Object value) {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    /**
     * 获取对象的私有字段值
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static <T> T getField(Object target, String fieldName) {
        return (T) ReflectionTestUtils.getField(target, fieldName);
    }
}
