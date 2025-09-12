package com.example.demo.controller;

import com.example.demo.entity.WhitelistApplication;
import com.example.demo.entity.AdminUser;
import com.example.demo.service.WhitelistApplicationService;
import com.example.demo.service.AdminUserService;
import com.example.demo.service.RconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private WhitelistApplicationService whitelistService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RconService rconService;

    // 检查登录状态的方法
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("adminUser") != null;
    }

    @GetMapping("")
    public String adminDashboard(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<WhitelistApplication> applications = whitelistService.getAllApplications();
        model.addAttribute("applications", applications);
        model.addAttribute("currentUser", session.getAttribute("adminUser"));

        return "admin/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editApplication(@PathVariable Long id, Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<WhitelistApplication> application = whitelistService.getApplicationById(id);
        if (application.isPresent()) {
            model.addAttribute("application", application.get());
            return "admin/edit";
        }
        return "redirect:/admin";
    }

    @PostMapping("/update")
    public String updateApplication(WhitelistApplication application, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            whitelistService.updateApplication(application);
            redirectAttributes.addFlashAttribute("success", "申请信息已成功更新！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败，请稍后重试。");
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteApplication(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            whitelistService.deleteApplication(id);
            redirectAttributes.addFlashAttribute("success", "申请已成功删除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败，请稍后重试。");
        }
        return "redirect:/admin";
    }

    // 管理员账号管理
    @GetMapping("/users")
    public String adminUsers(Model model, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<AdminUser> adminUsers = adminUserService.getAllAdmins();
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("newAdmin", new AdminUser());
        model.addAttribute("currentUser", session.getAttribute("adminUser"));

        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createAdminUser(@RequestParam String username, @RequestParam String password,
                                RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            adminUserService.createAdmin(username, password);
            redirectAttributes.addFlashAttribute("success", "管理员账号创建成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败：" + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteAdminUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            // 检查是否为ID为1的超级管理员
            if (id == 1L) {
                redirectAttributes.addFlashAttribute("error", "无法删除超级管理员账号！");
                return "redirect:/admin/users";
            }

            String currentUser = (String) session.getAttribute("adminUser");
            Optional<AdminUser> adminUser = adminUserService.getAdminById(id);

            if (adminUser.isPresent() && adminUser.get().getUsername().equals(currentUser)) {
                redirectAttributes.addFlashAttribute("error", "无法删除当前登录的管理员账号！");
            } else {
                adminUserService.deleteAdmin(id);
                redirectAttributes.addFlashAttribute("success", "管理员账号已成功删除！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败，请稍后重试。");
        }
        return "redirect:/admin/users";
    }

    // 直接修改指定用户密码（用于管理员操作）
    @PostMapping("/users/change-password/{id}")
    public String changeUserPassword(@PathVariable Long id,
                                   @RequestParam String newPassword,
                                   @RequestParam String confirmPassword,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            // 验证新密码和确认密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "新密码和确认密码不一致！");
                return "redirect:/admin/users";
            }

            // 验证新密码长度
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "新密码长度至少需要6位！");
                return "redirect:/admin/users";
            }

            // 获取要修改密码的用户
            Optional<AdminUser> adminUser = adminUserService.getAdminById(id);
            if (adminUser.isPresent()) {
                // 直接修改指定用户的密码
                adminUserService.changePassword(adminUser.get().getUsername(), newPassword);
                redirectAttributes.addFlashAttribute("success",
                    "用户 " + adminUser.get().getUsername() + " 的密码修改成功！");
            } else {
                redirectAttributes.addFlashAttribute("error", "用户不存在！");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "密码修改失败，请稍后重试。");
        }

        return "redirect:/admin/users";
    }

    // RCON白名单管理功能
    @PostMapping("/whitelist/add/{id}")
    public String addToWhitelist(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            Optional<WhitelistApplication> applicationOpt = whitelistService.getApplicationById(id);
            if (applicationOpt.isPresent()) {
                WhitelistApplication application = applicationOpt.get();
                String playerName = application.getPlayerName();
                String result = rconService.addToWhitelist(playerName);

                // 更新申请状态为"已通过"
                application.setWhitelistStatus("已通过");
                whitelistService.updateApplication(application);

                redirectAttributes.addFlashAttribute("success", "RCON执行结果: " + result);
            } else {
                redirectAttributes.addFlashAttribute("error", "找不到该申请记录！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "执行失败：" + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/whitelist/remove/{id}")
    public String removeFromWhitelist(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        try {
            Optional<WhitelistApplication> applicationOpt = whitelistService.getApplicationById(id);
            if (applicationOpt.isPresent()) {
                WhitelistApplication application = applicationOpt.get();
                String playerName = application.getPlayerName();
                String result = rconService.removeFromWhitelist(playerName);

                // 更新申请状态为"已拒绝"
                application.setWhitelistStatus("已拒绝");
                whitelistService.updateApplication(application);

                redirectAttributes.addFlashAttribute("success", "RCON执行结果: " + result);
            } else {
                redirectAttributes.addFlashAttribute("error", "找不到该申请记录！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "执行失败：" + e.getMessage());
        }
        return "redirect:/admin";
    }

    // RCON连接测试
    @GetMapping("/rcon/test")
    @ResponseBody
    public String testRconConnection(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "未授权访问";
        }

        boolean connected = rconService.testConnection();
        return connected ? "RCON连接正常" : "RCON连接失败";
    }

    @GetMapping("/rcon/status")
    @ResponseBody
    public String getWhitelistStatus(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "未授权访问";
        }

        return rconService.getWhitelistStatus();
    }
}
