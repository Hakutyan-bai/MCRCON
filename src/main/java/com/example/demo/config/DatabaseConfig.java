package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Autowired
    private ConfigManager configManager;

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(configManager.getDatabaseUrl())
                .username(configManager.getDatabaseUsername())
                .password(configManager.getDatabasePassword())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
