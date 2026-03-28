package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.User;
import poly.edu.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthAPIController {

    @Autowired
    private UserService userService;

    // ===== 1. LOGIN =====
    @PostMapping("/login")
    public Object login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session) {

        User user = userService.login(username, password);

        if (user == null) {
            return "Sai tài khoản hoặc mật khẩu";
        }

        session.setAttribute("user", user);

        return user;
    }

    // ===== 2. REGISTER =====
    @PostMapping("/register")
    public Object register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("email") String email) {

        User existingUser = userService.login(username, password);

        if (existingUser != null) {
            return "Tên đăng nhập đã tồn tại";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("USER");
        user.setEnabled(true);
        user.setFullName(username);

        return userService.register(user);
    }

    // ===== 3. LOGOUT =====
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "Logout thành công";
    }

    // ===== 4. FORGOT PASSWORD =====
    @PostMapping("/forgot")
    public String forgotPassword(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {

        User user = userService.findByUsername(username);

        if (user == null || !user.getEmail().equals(email)) {
            return "Sai username hoặc email";
        }

        user.setPassword(newPassword);
        userService.update(user);

        return "Đổi mật khẩu thành công";
    }

    // ===== 5. CHANGE PASSWORD =====
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "Chưa đăng nhập";
        }

        if (!user.getPassword().equals(oldPassword)) {
            return "Sai mật khẩu cũ";
        }

        user.setPassword(newPassword);
        userService.update(user);

        session.setAttribute("user", user);

        return "Đổi mật khẩu thành công";
    }
}