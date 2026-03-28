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
    
    
    // Hiển thị danh sách user
    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("user", new User());
        return "admin/users";
    }

    // Thêm user
    @RestController
    @RequestMapping("/api/admin/users")
    public class UserAPIController {

        @Autowired
        UserRepository userRepo;

        @PostMapping("/save")
        public User save(@RequestBody User user) {
            return userRepo.save(user);
        }
    }

    // Edit user
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
    	 User user = userRepo.findById(id).orElse(new User());
        model.addAttribute("user", userRepo.findById(id).orElse(null));
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}