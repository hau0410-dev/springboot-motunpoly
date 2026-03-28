package poly.edu.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Review;
import poly.edu.entity.User;
import poly.edu.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class UserReviewAPIController {

    @Autowired
    private ReviewService reviewService;

    // ===== 1. LƯU REVIEW =====
    @PostMapping("/save")
    public Object saveReview(@RequestBody Review request,
                             HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "Chưa đăng nhập";
        }

        Review review = new Review();
        review.setUsername(user.getUsername());
        review.setFullname(user.getFullName());
        review.setEmail(user.getEmail());
        review.setProductName(request.getProductName());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewService.save(review);

        return "Review thành công";
    }

    // ===== 2. LỊCH SỬ REVIEW =====
    @GetMapping
    public Object myReviews(HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "Chưa đăng nhập";
        }

        List<Review> list = reviewService.findByUsername(user.getUsername());

        return list;
    }
}