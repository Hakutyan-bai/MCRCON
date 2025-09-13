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
            // 新增：为旧版本配置文件补齐邮件配置
            ensureMailConfigExists();
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
        defaultProps.setProperty("database.name", "your_database");
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

        // 邮件配置（默认关闭，使用网易邮箱SMTP；授权码需自行填写）
        defaultProps.setProperty("mail.enabled", "false");
        defaultProps.setProperty("mail.host", "smtp.163.com");
        defaultProps.setProperty("mail.port", "465");
        defaultProps.setProperty("mail.username", "your_account@163.com");
        defaultProps.setProperty("mail.password", "your_163_auth_code");
        defaultProps.setProperty("mail.from", "your_account@163.com");
        defaultProps.setProperty("mail.ssl", "true");
        defaultProps.setProperty("mail.timeout", "10000");

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
            writer.write("rcon.timeout=" + defaultProps.getProperty("rcon.timeout") + "\n\n");

            writer.write("# ========== 邮件通知（审核结果）配置 ==========\n");
            writer.write("# 启用后将给玩家邮箱发送审核结果通知；邮箱需开启SMTP并使用授权码\n");
            writer.write("mail.enabled=" + defaultProps.getProperty("mail.enabled") + "\n");
            writer.write("mail.host=" + defaultProps.getProperty("mail.host") + "\n");
            writer.write("mail.port=" + defaultProps.getProperty("mail.port") + "\n");
            writer.write("mail.username=" + defaultProps.getProperty("mail.username") + "\n");
            writer.write("mail.password=" + defaultProps.getProperty("mail.password") + "\n");
            writer.write("mail.from=" + defaultProps.getProperty("mail.from") + "\n");
            writer.write("mail.ssl=" + defaultProps.getProperty("mail.ssl") + "\n");
            writer.write("mail.timeout=" + defaultProps.getProperty("mail.timeout") + "\n");
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

    // 新增：补齐缺失的邮件配置项
    private void ensureMailConfigExists() {
        String[][] defaults = new String[][]{
                {"mail.enabled", "false"},
                {"mail.host", "smtp.163.com"},
                {"mail.port", "465"},
                {"mail.username", "your_account@163.com"},
                {"mail.password", "your_163_auth_code"},
                {"mail.from", "your_account@163.com"},
                {"mail.ssl", "true"},
                {"mail.timeout", "10000"}
        };

        StringBuilder appendBuf = new StringBuilder();
        boolean needAppendHeader = false;

        for (String[] kv : defaults) {
            String key = kv[0];
            String def = kv[1];
            if (configProperties.getProperty(key) == null) {
                configProperties.setProperty(key, def);
                if (!needAppendHeader) {
                    needAppendHeader = true;
                    appendBuf.append("\n# ========== 邮件通知（审核结果）配置（自动补齐）==========\n");
                    appendBuf.append("# 修改为您自己的网易邮箱账号与授权码，并将 mail.enabled 设置为 true\n");
                }
                appendBuf.append(key).append("=").append(def).append("\n");
            }
        }

        if (needAppendHeader) {
            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8, true)) {
                writer.write(appendBuf.toString());
                System.out.println("已为配置文件追加缺失的邮件配置项");
            } catch (IOException e) {
                System.err.println("追加邮件配置失败: " + e.getMessage());
            }
        }
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

    // 邮件
    public boolean isMailEnabled() {
        return Boolean.parseBoolean(configProperties.getProperty("mail.enabled", "false"));
    }

    public String getMailHost() {
        return configProperties.getProperty("mail.host", "smtp.163.com");
    }

    public int getMailPort() {
        return Integer.parseInt(configProperties.getProperty("mail.port", "465"));
    }

    public String getMailUsername() {
        return configProperties.getProperty("mail.username", "");
    }

    public String getMailPassword() {
        return configProperties.getProperty("mail.password", "");
    }

    public String getMailFrom() {
        return configProperties.getProperty("mail.from", getMailUsername());
    }

    public String getMailFromName() {
        return configProperties.getProperty("mail.fromName", "MC白名单系统");
    }

    public String getMailCharset() {
        return configProperties.getProperty("mail.charset", "UTF-8");
    }

    public boolean isMailSsl() {
        return Boolean.parseBoolean(configProperties.getProperty("mail.ssl", "true"));
    }

    public int getMailTimeout() {
        return Integer.parseInt(configProperties.getProperty("mail.timeout", "10000"));
    }

    // 构建数据库连接URL
    public String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true&sslMode=DISABLED",
                getDatabaseHost(), getDatabasePort(), getDatabaseName());
    }
}
