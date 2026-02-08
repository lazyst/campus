package com.campus.config;

import com.baomidou.dynamic.datasource.annotation.DS;
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

/**
 * 数据源切换AOP切面配置
 * 确保@DS注解在@Transactional之前执行
 * 解决读写分离与事务的冲突问题
 *
 * @author campus
 * @date 2024-02-03
 */
@Aspect
@Component
@Order(0) // 确保在事务切面之前执行
public class DsAspectConfig {

    private static final Logger log = LoggerFactory.getLogger(DsAspectConfig.class);

    /**
     * 环绕通知：处理@DS注解的数据源切换
     *
     * @param point 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(com.baomidou.dynamic.datasource.annotation.DS)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取目标方法上的@DS注解
        DS dsAnnotation = null;
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            dsAnnotation = method.getAnnotation(DS.class);
        } catch (NullPointerException e) {
            log.warn("无法获取方法上的@DS注解，使用默认数据源");
            return point.proceed();
        }

        if (dsAnnotation != null) {
            String dataSourceKey = dsAnnotation.value();
            log.debug("切换数据源到: {}", dataSourceKey);
            
            // 切换数据源
            DynamicDataSourceContextHolder.push(dataSourceKey);
            try {
                return point.proceed();
            } finally {
                // 清除数据源上下文，防止内存泄漏
                DynamicDataSourceContextHolder.poll();
                log.debug("数据源切换完成，已清除上下文");
            }
        }
        
        return point.proceed();
    }
}
