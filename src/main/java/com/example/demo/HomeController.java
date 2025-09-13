package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.WhitelistApplication;
import com.example.demo.service.WhitelistApplicationService;
import com.example.demo.service.EmailService;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Pattern;

@Controller
public class HomeController {

    @Autowired
    private WhitelistApplicationService whitelistService;

    @Autowired
    private EmailService emailService;

    private static final String EMAIL_VERIFY_SESSION_KEY = "EMAIL_VERIFY";
    private static final long VERIFY_EXPIRE_MS = 10 * 60 * 1000; // 10分钟
    private static final long SEND_INTERVAL_MS = 60 * 1000; // 60秒内不允许重复发送
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/apply")
    public String apply(Model model) {
        model.addAttribute("application", new WhitelistApplication());
        return "apply";
    }

    // 发送邮箱验证码
    @PostMapping("/apply/send-code")
    @ResponseBody
    public Map<String, Object> sendVerifyCode(@RequestParam String email, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            resp.put("success", false);
            resp.put("message", "请输入有效的邮箱地址");
            return resp;
        }

        // 频率限制
        Map<String, Map<String, Object>> store = getOrInitVerifyStore(session);
        Map<String, Object> info = store.get(email);
        long now = System.currentTimeMillis();
        if (info != null) {
            Long lastSent = (Long) info.getOrDefault("sentAt", 0L);
            if (now - lastSent < SEND_INTERVAL_MS) {
                long wait = (SEND_INTERVAL_MS - (now - lastSent)) / 1000;
                resp.put("success", false);
                resp.put("message", "发送过于频繁，请" + wait + "秒后再试");
                return resp;
            }
        }

        // 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        Map<String, Object> record = new HashMap<>();
        record.put("code", code);
        record.put("sentAt", now);
        store.put(email, record);
        session.setAttribute(EMAIL_VERIFY_SESSION_KEY, store);

        // 发送邮件
        emailService.sendVerificationCode(email, code);

        resp.put("success", true);
        resp.put("message", "验证码已发送，请检查邮箱");
        return resp;
    }

    @PostMapping("/apply")
    public String submitApplication(WhitelistApplication application,
                                    @RequestParam(name = "verificationCode", required = false) String verificationCode,
                                    Model model,
                                    HttpSession session) {
        try {
            // 基础输入验证
            if (application.getPlayerName() == null || application.getPlayerName().trim().isEmpty()) {
                model.addAttribute("error", "玩家ID不能为空！");
                return "apply-result";
            }

            if (application.getEmail() == null || application.getEmail().trim().isEmpty()
                    || !EMAIL_PATTERN.matcher(application.getEmail()).matches()) {
                model.addAttribute("error", "请输入有效的邮箱地址！");
                return "apply-result";
            }

            if (application.getQqNumber() == null || application.getQqNumber().trim().isEmpty()) {
                model.addAttribute("error", "QQ号码不能为空！");
                return "apply-result";
            }

            // 取消申请原因必填；为兼容旧表非空约束，空时写入空字符串
            if (application.getReason() == null) {
                application.setReason("");
            }

            // 校验邮箱验证码
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                model.addAttribute("error", "请填写邮箱验证码！");
                return "apply-result";
            }
            Map<String, Map<String, Object>> store = getOrInitVerifyStore(session);
            Map<String, Object> info = store.get(application.getEmail());
            if (info == null) {
                model.addAttribute("error", "请先获取邮箱验证码！");
                return "apply-result";
            }
            String codeSaved = String.valueOf(info.get("code"));
            Long sentAt = (Long) info.getOrDefault("sentAt", 0L);
            if (!verificationCode.trim().equals(codeSaved)) {
                model.addAttribute("error", "验证码不正确！");
                return "apply-result";
            }
            if (System.currentTimeMillis() - sentAt > VERIFY_EXPIRE_MS) {
                model.addAttribute("error", "验证码已过期，请重新获取！");
                return "apply-result";
            }

            // 检查玩家名是否已存在
            if (whitelistService.isPlayerNameExists(application.getPlayerName())) {
                model.addAttribute("error", "该玩家名已经申请过白名单！");
                model.addAttribute("playerName", application.getPlayerName());
                return "apply-result";
            }

            // 检查邮箱是否已存在
            if (whitelistService.isEmailExists(application.getEmail())) {
                model.addAttribute("error", "该邮箱已经申请过白名单！");
                model.addAttribute("email", application.getEmail());
                return "apply-result";
            }

            // 保存申请
            whitelistService.saveApplication(application);
            model.addAttribute("success", "申请提交成功！我们会尽快审核您的申请。");
            model.addAttribute("playerName", application.getPlayerName());

            // 使用后清理验证码，避免重复提交
            store.remove(application.getEmail());
            session.setAttribute(EMAIL_VERIFY_SESSION_KEY, store);

        } catch (Exception e) {
            model.addAttribute("error", "申请提交失败：" + e.getMessage() + "，请稍后重试。");
            e.printStackTrace();
        }

        return "apply-result";
    }

    // 玩家修改申请信息页面
    @GetMapping("/edit-application/{playerName}")
    public String editApplication(@PathVariable String playerName, Model model) {
        WhitelistApplication existingApplication = whitelistService.getApplicationByPlayerName(playerName);

        if (existingApplication != null) {
            model.addAttribute("application", existingApplication);
            model.addAttribute("isEdit", true);
        } else {
            return "redirect:/apply";
        }

        return "edit-application";
    }

    // 处理玩家修改申请信息
    @PostMapping("/edit-application")
    public String updateApplication(WhitelistApplication application, Model model) {
        try {
            WhitelistApplication existingApplication = whitelistService.getApplicationByPlayerName(application.getPlayerName());
            if (existingApplication == null) {
                model.addAttribute("error", "找不到该玩家的申请记录！");
                return "edit-application";
            }

            // 保持原有的ID和创建时间
            application.setId(existingApplication.getId());
            application.setCreatedTime(existingApplication.getCreatedTime());

            // 兼容旧表非空约束
            if (application.getReason() == null) {
                application.setReason("");
            }

            // 更新申请信息
            whitelistService.updateApplication(application);
            model.addAttribute("success", "申请信息修改成功！");
            model.addAttribute("playerName", application.getPlayerName());

        } catch (Exception e) {
            model.addAttribute("error", "申请信息修改失败，请稍后重试。");
        }

        return "apply-result";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getOrInitVerifyStore(HttpSession session) {
        Object obj = session.getAttribute(EMAIL_VERIFY_SESSION_KEY);
        if (obj instanceof Map) {
            return (Map<String, Map<String, Object>>) obj;
        }
        Map<String, Map<String, Object>> store = new HashMap<>();
        session.setAttribute(EMAIL_VERIFY_SESSION_KEY, store);
        return store;
    }
}
