package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Autowired
    private ConfigManager configManager;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(configManager.getMailHost());
        mailSender.setPort(configManager.getMailPort());
        mailSender.setUsername(configManager.getMailUsername());
        mailSender.setPassword(configManager.getMailPassword());
        mailSender.setDefaultEncoding(configManager.getMailCharset());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", String.valueOf(configManager.getMailTimeout()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(configManager.getMailTimeout()));
        props.put("mail.debug", "false");

        // 字符集与编码设置，确保中文发件人/主题不乱码
        props.put("mail.mime.charset", configManager.getMailCharset());
        props.put("mail.mime.encodefilename", "true");
        props.put("mail.mime.encodeparameters", "true");
        props.put("mail.mime.address.strict", "false");
        // 某些环境可开启 UTF-8 地址，但大多数 SMTP 不支持，保持默认 false
        // props.put("mail.mime.allowutf8", "true");

        if (configManager.isMailSsl()) {
            // 465端口通常需要SSL
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            // 587端口通常使用STARTTLS
            props.put("mail.smtp.starttls.enable", "true");
        }

        return mailSender;
    }
}
