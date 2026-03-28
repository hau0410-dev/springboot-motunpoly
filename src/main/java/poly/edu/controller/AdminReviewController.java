package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.service.ReviewService;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @Autowired
    ReviewService reviewService;

    
    @GetMapping
    public String list(
            @RequestParam(name = "user", required = false) String user,
            @RequestParam(name = "product", required = false) String product,
            Model model) {

        if (user == null && product == null) {
            model.addAttribute("reviews", reviewService.findAll());
        } else {
            model.addAttribute("reviews",
                    reviewService.search(user, product));
        }

        return "admin/reviews";
    }
    // Xoá review
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        reviewService.deleteById(id);
        return "redirect:/admin/reviews";
    }
}