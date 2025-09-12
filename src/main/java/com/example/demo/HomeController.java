package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.WhitelistApplication;
import com.example.demo.service.WhitelistApplicationService;

@Controller
public class HomeController {

    @Autowired
    private WhitelistApplicationService whitelistService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/apply")
    public String apply(Model model) {
        model.addAttribute("application", new WhitelistApplication());
        return "apply";
    }

    @PostMapping("/apply")
    public String submitApplication(WhitelistApplication application, Model model) {
        try {
            // 添加输入验证
            if (application.getPlayerName() == null || application.getPlayerName().trim().isEmpty()) {
                model.addAttribute("error", "玩家ID不能为空！");
                return "apply-result";
            }

            if (application.getEmail() == null || application.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "邮箱地址不能为空！");
                return "apply-result";
            }

            if (application.getQqNumber() == null || application.getQqNumber().trim().isEmpty()) {
                model.addAttribute("error", "QQ号码不能为空！");
                return "apply-result";
            }

            if (application.getReason() == null || application.getReason().trim().isEmpty()) {
                model.addAttribute("error", "申请原因不能为空！");
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

        } catch (Exception e) {
            // 添加更详细的错误信息
            model.addAttribute("error", "申请提交失败：" + e.getMessage() + "，请稍后重试。");
            e.printStackTrace(); // 在控制台打印详细错误信息
        }

        return "apply-result";
    }

    // 玩家修改申请信息页面
    @GetMapping("/edit-application/{playerName}")
    public String editApplication(@PathVariable String playerName, Model model) {
        // 根据玩家名查找现有申请
        WhitelistApplication existingApplication = whitelistService.getApplicationByPlayerName(playerName);

        if (existingApplication != null) {
            model.addAttribute("application", existingApplication);
            model.addAttribute("isEdit", true);
        } else {
            // 如果找不到申请，重定向到申请页面
            return "redirect:/apply";
        }

        return "edit-application";
    }

    // 处理玩家修改申请信息
    @PostMapping("/edit-application")
    public String updateApplication(WhitelistApplication application, Model model) {
        try {
            // 验证申请是否存在
            WhitelistApplication existingApplication = whitelistService.getApplicationByPlayerName(application.getPlayerName());
            if (existingApplication == null) {
                model.addAttribute("error", "找不到该玩家的申请记录！");
                return "edit-application";
            }

            // 保持原有的ID和创建时间
            application.setId(existingApplication.getId());
            application.setCreatedTime(existingApplication.getCreatedTime());

            // 更新申请信息
            whitelistService.updateApplication(application);
            model.addAttribute("success", "申请信息修改成功！");
            model.addAttribute("playerName", application.getPlayerName());

        } catch (Exception e) {
            model.addAttribute("error", "申请信息修改失败，请稍后重试。");
        }

        return "apply-result";
    }

}
