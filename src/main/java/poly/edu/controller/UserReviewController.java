package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Review;
import poly.edu.entity.User;
import poly.edu.service.ReviewService;

@Controller
public class UserReviewController {

    @Autowired
    ReviewService reviewService;

    // Lưu review
    @PostMapping("/review/save")
    public String saveReview(
            @RequestParam("productName") String productName,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        // CHỐNG NULL
        if (user == null) {
            return "redirect:/login";
        }

        Review review = new Review();
        review.setUsername(user.getUsername());
        review.setFullname(user.getFullName());
        review.setEmail(user.getEmail());
        review.setProductName(productName);
        review.setRating(rating);
        review.setComment(comment);

        reviewService.save(review);

        return "redirect:/orders";
    }
    // Lịch sử review
    @GetMapping("/reviews")
    public String myReviews(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("reviews",
                reviewService.findByUsername(user.getUsername()));

        return "user/reviews";
    }
}