package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.User;
import poly.edu.repository.UserRepository;
import poly.edu.service.UserService;

@RestController
@RequestMapping("/api/profile")
public class ProfileAPIController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository repo;

    // ===== 1. XEM PROFILE =====
    @GetMapping
    public Object profile(HttpSession session) {

        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            return "Chưa đăng nhập";
        }

        User user = userService.findByUsername(userSession.getUsername());

        return user;
    }

    // ===== 2. UPDATE PROFILE =====
    @PutMapping("/update")
    public Object update(@RequestBody User formUser,
                         HttpSession session) {

        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            return "Chưa đăng nhập";
        }

        User user = repo.findById(formUser.getId()).orElse(null);

        if (user != null) {

            user.setFullName(formUser.getFullName());
            user.setEmail(formUser.getEmail());
            user.setAvatar(formUser.getAvatar());

            repo.save(user);

            return "Cập nhật thành công";
        }

        return "User không tồn tại";
    }
}