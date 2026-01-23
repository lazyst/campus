package com.campus.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 测试配置类
 * 提供测试专用的数据库配置和数据源
 */
@TestConfiguration
public class TestConfig {

    /**
     * 测试用数据源 - 连接到真实的MySQL数据库
     */
    @Bean
    @Primary
    public DataSource testDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/campus_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("123");
        return dataSource;
    }
}
