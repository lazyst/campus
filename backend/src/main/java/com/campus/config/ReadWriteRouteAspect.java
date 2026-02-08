package com.campus.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读写分离自动路由AOP切面
 * 根据方法名自动识别读写操作并路由到对应的数据源
 * 
 * 路由规则：
 * - 读操作方法（get/find/list/query/search/select/count/sum/avg...）：走从库
 * - 写操作方法（save/add/create/update/delete/remove/modify...）：走主库
 * - 默认：走主库
 * 
 * @author campus
 * @date 2024-02-03
 */
@Aspect
@Component
@Order(1) // 在DynamicDataSource的切面之后执行
public class ReadWriteRouteAspect {

    private static final Logger log = LoggerFactory.getLogger(ReadWriteRouteAspect.class);

    // 读操作方法名匹配模式
    private static final Pattern READ_PATTERN = Pattern.compile(
        "^(get|find|list|query|search|select|count|sum|avg|min|max|check|verify|is|has|exist|load|fetch|retrieve|page|queryBy|findBy|getBy).*",
        Pattern.CASE_INSENSITIVE
    );

    // 写操作方法名匹配模式
    private static final Pattern WRITE_PATTERN = Pattern.compile(
        "^(save|add|create|update|delete|remove|modify|insert|change|alter|drop|truncate|reset|clear|cancel|submit|publish|cancel).*",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 环绕通知：自动路由到对应的数据源
     */
    @Around("execution(* com.campus.modules.*.service..*(..))")
    public Object route(ProceedingJoinPoint point) throws Throwable {
        // 获取目标方法
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();

        // 跳过已经使用@DS注解的方法
        if (method.isAnnotationPresent(com.baomidou.dynamic.datasource.annotation.DS.class)) {
            return point.proceed();
        }

        // 确定数据源
        String dataSourceKey = determineDataSource(methodName);
        
        log.debug("读写分离路由 - 类: {}, 方法: {}, 数据源: {}", 
                className, methodName, dataSourceKey);

        // 切换数据源
        DynamicDataSourceContextHolder.push(dataSourceKey);
        try {
            return point.proceed();
        } finally {
            // 清除数据源上下文
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 根据方法名确定数据源
     * 
     * @param methodName 方法名
     * @return 数据源标识（master/slave）
     */
    private String determineDataSource(String methodName) {
        // 先检查是否是写操作
        Matcher writeMatcher = WRITE_PATTERN.matcher(methodName);
        if (writeMatcher.matches()) {
            return "master";
        }

        // 再检查是否是读操作
        Matcher readMatcher = READ_PATTERN.matcher(methodName);
        if (readMatcher.matches()) {
            return "slave";
        }

        // 默认走主库（安全策略：不确定时走主库）
        return "master";
    }
}
