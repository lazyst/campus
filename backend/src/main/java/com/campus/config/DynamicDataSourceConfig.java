package com.campus.config;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源配置类
 * 实现数据库读写分离：主库写操作，从库读操作
 * 
 * @author campus
 * @date 2024-02-03
 */
@Configuration
public class DynamicDataSourceConfig {

    @Autowired
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    /**
     * 配置主数据源（写库）
     * 使用@Primary确保默认使用主库
     */
    @Bean
    @Primary
    public DataSource masterDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        // 从配置中读取主库信息
        Map<String, DataSourceProperty> datasourceMap = dynamicDataSourceProperties.getDatasource();
        DataSourceProperty masterProperties = datasourceMap.get("master");
        
        if (masterProperties != null) {
            dataSource.setJdbcUrl(masterProperties.getUrl());
            dataSource.setUsername(masterProperties.getUsername());
            dataSource.setPassword(masterProperties.getPassword());
            dataSource.setDriverClassName(masterProperties.getDriverClassName());
            
            // 配置HikariCP连接池
            dataSource.setMinimumIdle(5);
            dataSource.setMaximumPoolSize(20);
            dataSource.setIdleTimeout(30000);
            dataSource.setPoolName("CampusHikariCP-Master");
            dataSource.setMaxLifetime(1800000);
            dataSource.setConnectionTimeout(30000);
        }
        
        return dataSource;
    }

    /**
     * 配置从数据源（读库）
     */
    @Bean
    public DataSource slaveDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        // 从配置中读取从库信息
        Map<String, DataSourceProperty> datasourceMap = dynamicDataSourceProperties.getDatasource();
        DataSourceProperty slaveProperties = datasourceMap.get("slave");
        
        if (slaveProperties != null) {
            dataSource.setJdbcUrl(slaveProperties.getUrl());
            dataSource.setUsername(slaveProperties.getUsername());
            dataSource.setPassword(slaveProperties.getPassword());
            dataSource.setDriverClassName(slaveProperties.getDriverClassName());
            
            // 配置HikariCP连接池
            dataSource.setMinimumIdle(5);
            dataSource.setMaximumPoolSize(20);
            dataSource.setIdleTimeout(30000);
            dataSource.setPoolName("CampusHikariCP-Slave");
            dataSource.setMaxLifetime(1800000);
            dataSource.setConnectionTimeout(30000);
        }
        
        return dataSource;
    }
}
