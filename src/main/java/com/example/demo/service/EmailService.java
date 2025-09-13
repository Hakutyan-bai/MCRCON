package com.example.demo.service;

import com.example.demo.config.ConfigManager;
import com.example.demo.entity.WhitelistApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ConfigManager configManager;

    /**
     * 发送审核结果通知邮件（仅当 mail.enabled=true 时发送）
     */
    public void sendReviewResultEmail(WhitelistApplication application) {
        if (!configManager.isMailEnabled()) {
            return; // 未启用邮件功能
        }
        try {
            String to = application.getEmail();
            String player = application.getPlayerName();
            String status = application.getWhitelistStatus();

            String subject = "白名单审核结果通知";
            StringBuilder text = new StringBuilder();
            text.append("您好，玩家 ").append(player).append("：\n\n");
            text.append("您提交的白名单申请已处理，当前状态：").append(status).append("。\n");
            if ("已通过".equals(status)) {
                text.append("您已被加入服务器白名单，欢迎加入游戏！\n");
            } else if ("已拒绝".equals(status)) {
                text.append("很抱歉，您的白名单申请未通过。\n");
            } else {
                text.append("您的申请正在审核中，请耐心等待。\n");
            }
            text.append("\n— 本邮件由系统自动发送，请勿直接回复。");

            String charset = configManager.getMailCharset();

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, charset);
            // 直接使用邮箱地址作为发件人
            helper.setFrom(configManager.getMailFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text.toString(), false);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // 记录异常但不影响业务流程
            System.err.println("发送审核结果邮件失败: " + e.getMessage());
        }
    }

    /**
     * 发送邮箱验证码
     */
    public void sendVerificationCode(String to, String code) {
        if (!configManager.isMailEnabled()) {
            return;
        }
        try {
            String charset = configManager.getMailCharset();
            String subject = "邮箱验证代码";
            String text = "您的验证码为：" + code + "，10分钟内有效。\n\n— 本邮件由系统自动发送，请勿回复。";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, charset);
            helper.setFrom(configManager.getMailFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("发送验证码邮件失败: " + e.getMessage());
        }
    }
}
