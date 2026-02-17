package com.campus.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

/**
 * Service测试基类
 * 提供ServiceImpl测试所需的公共配置和工具方法
 *
 * 使用方式:
 * <pre>
 * {@code
 * @ExtendWith(MockitoExtension.class)
 * @MockitoSettings(strictness = Strictness.LENIENT)
 * class MyServiceTest extends ServiceTestBase {
 *
 *     @Mock
 *     private MyMapper myMapper;
 *
 *     @InjectMocks
 *     private MyServiceImpl myService;
 *
 *     @BeforeEach
 *     void setUp() {
 *         setBaseMapper(myService, myMapper);
 *     }
 * }
 * }
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class ServiceTestBase {

    /**
     * 为ServiceImpl注入baseMapper
     * 解决Mockito无法自动注入继承字段的问题
     *
     * @param service    ServiceImpl实例
     * @param baseMapper Mock的BaseMapper实例
     * @param <M>        Mapper类型
     * @param <T>        实体类型
     */
    protected <M extends BaseMapper<T>, T> void setBaseMapper(
            ServiceImpl<M, T> service,
            M baseMapper) {
        ReflectionTestUtils.setField(service, "baseMapper", baseMapper);
    }

    /**
     * 使用反射设置对象的私有字段
     *
     * @param target     目标对象
     * @param fieldName 字段名
     * @param value     字段值
     */
    protected void setField(Object target, String fieldName, Object value) {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    /**
     * 获取对象的私有字段值
     *
     * @param target    目标对象
     * @param fieldName 字段名
     * @param <T>      字段类型
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    protected <T> T getField(Object target, String fieldName) {
        return (T) ReflectionTestUtils.getField(target, fieldName);
    }

    /**
     * 使用反射设置baseMapper（传统方式，作为备选）
     *
     * @param service    ServiceImpl实例
     * @param baseMapper Mock的BaseMapper实例
     * @param <M>        Mapper类型
     * @param <T>        实体类型
     */
    protected <M extends BaseMapper<T>, T> void setBaseMapperReflectively(
            ServiceImpl<M, T> service,
            M baseMapper) {
        try {
            Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(service, baseMapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set baseMapper: " + e.getMessage(), e);
        }
    }
}
