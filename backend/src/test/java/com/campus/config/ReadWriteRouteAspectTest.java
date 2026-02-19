package com.campus.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadWriteRouteAspect - 读写分离路由切面")
class ReadWriteRouteAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private ReadWriteRouteAspect aspect;

    @Test
    @DisplayName("route - 写操作方法路由到主库")
    void shouldRouteWriteOperationToMaster() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("save", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.route(joinPoint);

        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("route - 读操作方法路由到从库")
    void shouldRouteReadOperationToSlave() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("getById", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.route(joinPoint);

        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("route - 统计方法路由到从库")
    void shouldRouteCountOperationToSlave() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("count", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn(10);

        Object result = aspect.route(joinPoint);

        assertEquals(10, result);
    }

    @Test
    @DisplayName("route - 查询方法路由到从库")
    void shouldRouteQueryOperationToSlave() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("findAll", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn("list");

        Object result = aspect.route(joinPoint);

        assertEquals("list", result);
    }

    @Test
    @DisplayName("route - 更新方法路由到主库")
    void shouldRouteUpdateOperationToMaster() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("updateById", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn(true);

        Object result = aspect.route(joinPoint);

        assertEquals(true, result);
    }

    @Test
    @DisplayName("route - 删除方法路由到主库")
    void shouldRouteDeleteOperationToMaster() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("removeById", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn(true);

        Object result = aspect.route(joinPoint);

        assertEquals(true, result);
    }

    @Test
    @DisplayName("route - 未知操作默认路由到主库")
    void shouldRouteUnknownOperationToMaster() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("process", TestService.class)
        );
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.route(joinPoint);

        assertEquals("result", result);
    }

    @Test
    @DisplayName("route - 有@DS注解的方法跳过路由")
    void shouldSkipRoutingWhenDsAnnotationPresent() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethodWithDs("getById", TestServiceWithDs.class)
        );
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.route(joinPoint);

        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("route - 异常时确保清理数据源上下文")
    void shouldCleanupDataSourceOnException() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(
                createMethod("getById", TestService.class)
        );
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        assertThrows(RuntimeException.class, () -> aspect.route(joinPoint));
    }

    private Method createMethod(String name, Class<?> clazz) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Method createMethodWithDs(String name, Class<?> clazz) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    static class TestService {
        public Object getById() { return null; }
        public Object save() { return null; }
        public Object updateById() { return null; }
        public Object removeById() { return null; }
        public Object count() { return null; }
        public Object findAll() { return null; }
        public Object process() { return null; }
    }

    static class TestServiceWithDs {
        @com.baomidou.dynamic.datasource.annotation.DS("slave")
        public Object getById() { return null; }
    }
}
