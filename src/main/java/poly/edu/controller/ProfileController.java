package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import jakarta.servlet.http.HttpSession;
import poly.edu.entity.User;
import poly.edu.repository.UserRepository;
import poly.edu.service.UserService;

@Controller
public class ProfileController {

    @Autowired
    UserService userService;
    
    @Autowired
    UserRepository repo;
    

    // HIỂN THỊ PROFILE
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        User userSession = (User) session.getAttribute("user");

        if (userSession == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userSession.getUsername());

        model.addAttribute("user", user);

        return "user/profile";
    }
    @PostMapping("/profile/update")
    public String update(User formUser,RedirectAttributes redirect) {

        User user = repo.findById(formUser.getId()).orElse(null);

        if(user != null){

            user.setFullName(formUser.getFullName());
            user.setEmail(formUser.getEmail());
            user.setAvatar(formUser.getAvatar());
            user.setBankName(formUser.getBankName());
            user.setBankAccount(formUser.getBankAccount());
            user.setBankHolder(formUser.getBankHolder());

            repo.save(user);
            redirect.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        }

        return "redirect:/profile";
    }
}