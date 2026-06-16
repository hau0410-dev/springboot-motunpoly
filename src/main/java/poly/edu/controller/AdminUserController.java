package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.User;
import poly.edu.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    UserRepository userRepo;

    // Danh sách user
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("user", new User());
        return "admin/users";
    }

    // Form thêm/sửa user (load sẵn dữ liệu)
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("user", userRepo.findById(id).orElse(new User()));
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    // Lưu user (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute User user) {
        userRepo.save(user);
        return "redirect:/admin/users";
    }

    // Xoá user
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}