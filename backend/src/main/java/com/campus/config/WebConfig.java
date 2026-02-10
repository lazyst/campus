package com.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置 - 静态资源映射
 * 上传路径通过配置文件注入，支持多环境
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 上传目录路径 - 通过配置文件注入
     * 开发环境: ./uploads
     * 生产环境: /app/uploads (Docker 卷挂载点)
     */
    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传目录映射为静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600); // 缓存1小时
    }
}
