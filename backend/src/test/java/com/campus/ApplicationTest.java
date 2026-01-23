package com.campus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用启动测试类
 * 验证Spring Boot应用上下文是否正确加载
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationTest {

    /**
     * 测试应用上下文加载
     * 验证所有配置和组件正确初始化
     */
    @Test
    void contextLoads() {
        // 应用上下文加载成功即表示测试通过
    }

    /**
     * 测试应用主类存在
     */
    @Test
    void mainClassExists() {
        CampusApplication application = new CampusApplication();
        // 主类实例化成功
    }
}
