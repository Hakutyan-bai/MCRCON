package com.example.demo.config;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class ConfigManager {

    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "config/application-custom.properties";
    private Properties configProperties;

    @PostConstruct
    public void initConfig() {
        try {
            createConfigDirectory();
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                createDefaultConfigFile();
            }
            loadConfigFile();
        } catch (Exception e) {
            System.err.println("配置文件初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createConfigDirectory() throws IOException {
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
            System.out.println("已创建配置目录: " + configDir.toAbsolutePath());
        }
    }

    private void createDefaultConfigFile() throws IOException {
        Properties defaultProps = new Properties();

        // 数据库配置
        defaultProps.setProperty("database.host", "localhost");
        defaultProps.setProperty("database.port", "3306");
        defaultProps.setProperty("database.name", "mcrcon");
        defaultProps.setProperty("database.username", "root");
        defaultProps.setProperty("database.password", "mysql_password");

        // 管理员默认账号配置
        defaultProps.setProperty("admin.default.username", "admin");
        defaultProps.setProperty("admin.default.password", "admin123");

        // RCON配置
        defaultProps.setProperty("rcon.host", "localhost");
        defaultProps.setProperty("rcon.port", "25575");
        defaultProps.setProperty("rcon.password", "rcon_password");
        defaultProps.setProperty("rcon.timeout", "3000");

        // 写入配置文件
        try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
            writer.write("# MC_RCON 白名单管理系统配置文件\n");
            writer.write("# 此文件在首次启动时自动生成，您可以根据需要修改配置\n");
            writer.write("# 修改后需要重启应用程序才能生效\n\n");

            writer.write("# ========== 数据库配置 ==========\n");
            writer.write("database.host=" + defaultProps.getProperty("database.host") + "\n");
            writer.write("database.port=" + defaultProps.getProperty("database.port") + "\n");
            writer.write("database.name=" + defaultProps.getProperty("database.name") + "\n");
            writer.write("database.username=" + defaultProps.getProperty("database.username") + "\n");
            writer.write("database.password=" + defaultProps.getProperty("database.password") + "\n\n");

            writer.write("# ========== 管理员默认账号配置 ==========\n");
            writer.write("admin.default.username=" + defaultProps.getProperty("admin.default.username") + "\n");
            writer.write("admin.default.password=" + defaultProps.getProperty("admin.default.password") + "\n\n");

            writer.write("# ========== RCON服务器配置 ==========\n");
            writer.write("rcon.host=" + defaultProps.getProperty("rcon.host") + "\n");
            writer.write("rcon.port=" + defaultProps.getProperty("rcon.port") + "\n");
            writer.write("rcon.password=" + defaultProps.getProperty("rcon.password") + "\n");
            writer.write("rcon.timeout=" + defaultProps.getProperty("rcon.timeout") + "\n");
        }

        System.out.println("已生成默认配置文件: " + Paths.get(CONFIG_FILE).toAbsolutePath());
        System.out.println("请根据您的环境修改配置文件中的参数");
    }

    private void loadConfigFile() throws IOException {
        configProperties = new Properties();
        try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            configProperties.load(reader);
        }
        System.out.println("已加载配置文件: " + CONFIG_FILE);
    }

    // Getter methods for configuration values
    public String getDatabaseHost() {
        return configProperties.getProperty("database.host", "localhost");
    }

    public String getDatabasePort() {
        return configProperties.getProperty("database.port", "3306");
    }

    public String getDatabaseName() {
        return configProperties.getProperty("database.name", "mcrcon");
    }

    public String getDatabaseUsername() {
        return configProperties.getProperty("database.username", "root");
    }

    public String getDatabasePassword() {
        return configProperties.getProperty("database.password", "");
    }

    public String getAdminDefaultUsername() {
        return configProperties.getProperty("admin.default.username", "admin");
    }

    public String getAdminDefaultPassword() {
        return configProperties.getProperty("admin.default.password", "admin123");
    }

    public String getRconHost() {
        return configProperties.getProperty("rcon.host", "127.0.0.1");
    }

    public int getRconPort() {
        return Integer.parseInt(configProperties.getProperty("rcon.port", "25575"));
    }

    public String getRconPassword() {
        return configProperties.getProperty("rcon.password", "");
    }

    public int getRconTimeout() {
        return Integer.parseInt(configProperties.getProperty("rcon.timeout", "3000"));
    }

    // 构建数据库连接URL
    public String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true&sslMode=DISABLED",
                getDatabaseHost(), getDatabasePort(), getDatabaseName());
    }
}
