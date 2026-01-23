package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园互助平台主应用类
 * Spring Boot 3 启动入口
 */
@SpringBootApplication
@MapperScan("com.campus.modules.**.mapper")
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
