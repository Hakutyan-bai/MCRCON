package com.example.demo.controller;

import com.example.demo.entity.AdminUser;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
public class LoginController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ConfigManager configManager;

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        // 如果已经登录，直接跳转到管理页面
        if (session.getAttribute("adminUser") != null) {
            return "redirect:/admin";
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password,
                         Model model, HttpSession session) {
        try {
            // 查找管理员用户
            AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);

            if (adminUser != null && verifyPassword(password, adminUser.getPassword())) {
                // 登录成功，将用户信息存储到session中
                session.setAttribute("adminUser", adminUser.getUsername());
                return "redirect:/admin";
            } else {
                model.addAttribute("error", true);
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", true);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 简单的密码验证（实际项目中应该使用更安全的加密方式）
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return hashPassword(rawPassword).equals(encodedPassword);
    }

    // 简单的密码哈希（实际项目中应该使用更安全的方式）
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 如果SHA-256不可用，直接返回原密码（仅用于演示）
            return password;
        }
    }

    // 初始化默认管理员账号
    @PostConstruct
    public void init() {
        String defaultUsername = configManager.getAdminDefaultUsername();
        String defaultPassword = configManager.getAdminDefaultPassword();

        if (!adminUserRepository.existsByUsername(defaultUsername)) {
            AdminUser defaultAdmin = new AdminUser();
            defaultAdmin.setUsername(defaultUsername);
            defaultAdmin.setPassword(hashPassword(defaultPassword));
            defaultAdmin.setRole("ADMIN");
            defaultAdmin.setEnabled(true);
            adminUserRepository.save(defaultAdmin);
            System.out.println("已创建默认管理员账号: " + defaultUsername);
        }
    }
}
