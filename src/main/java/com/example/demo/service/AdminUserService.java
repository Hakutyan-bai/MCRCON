package com.example.demo.service;

import com.example.demo.entity.AdminUser;
import com.example.demo.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    public AdminUser createAdmin(String username, String password) {
        if (adminUserRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setPassword(hashPassword(password));
        adminUser.setRole("ADMIN");
        adminUser.setEnabled(true);

        return adminUserRepository.save(adminUser);
    }

    public List<AdminUser> getAllAdmins() {
        return adminUserRepository.findAll();
    }

    public Optional<AdminUser> getAdminById(Long id) {
        return adminUserRepository.findById(id);
    }

    public Optional<AdminUser> getAdminByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public void deleteAdmin(Long id) {
        adminUserRepository.deleteById(id);
    }

    public AdminUser updateAdmin(AdminUser adminUser) {
        return adminUserRepository.save(adminUser);
    }

    public void changePassword(String username, String newPassword) {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        adminUser.setPassword(hashPassword(newPassword));
        adminUserRepository.save(adminUser);
    }

    public boolean existsByUsername(String username) {
        return adminUserRepository.existsByUsername(username);
    }

    // 验证当前密码是否正确
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        AdminUser adminUser = adminUserRepository.findByUsername(username).orElse(null);
        if (adminUser == null) {
            return false;
        }
        return hashPassword(currentPassword).equals(adminUser.getPassword());
    }

    // 简单的密码哈希（与LoginController中保持一致）
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
            return password;
        }
    }
}
