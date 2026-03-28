package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.User;
import poly.edu.service.UserService;

@Controller
public class AuthController {

    @Autowired
    UserService userService;   

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        // gọi DB để kiểm tra tài khoản
        User user = userService.login(username, password);

        if (user == null) {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
            return "auth/login";
        }

        // lưu user vào session
        session.setAttribute("user", user);

        // ✅ PHÂN QUYỀN ADMIN
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin";
        }

        // user thường
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("email") String email,
                           Model model,
                           RedirectAttributes redirectAttributes){

        // kiểm tra trùng username
        User existingUser = userService.login(username, password);
        if (existingUser != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "auth/register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("USER");
        user.setEnabled(true);
        user.setFullName(username);

        userService.register(user);

        redirectAttributes.addFlashAttribute("success",
                "Đăng ký thành công, hãy đăng nhập!");

        return "redirect:/login";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }
    @GetMapping("/forgot")
    public String forgotForm() {
        return "auth/forgot";
    }

    @PostMapping("/forgot")
    public String forgotPassword(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword,
            Model model) {

        User user = userService.findByUsername(username);

        if (user == null || !user.getEmail().equals(email)) {
            model.addAttribute("error", "Sai username hoặc email");
            return "auth/forgot";
        }

        user.setPassword(newPassword);
        userService.update(user);

        model.addAttribute("success", "Đổi mật khẩu thành công");
        return "auth/login";
    }
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("error", "Sai mật khẩu cũ");
            return "auth/change-password";
        }

        user.setPassword(newPassword);
        userService.update(user);

        session.setAttribute("user", user);

        model.addAttribute("success", "Đổi mật khẩu thành công");
        return "auth/change-password";
    }
}
