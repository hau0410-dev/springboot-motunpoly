package poly.edu.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Promotion;
import poly.edu.entity.User;
import poly.edu.service.PromotionService;

@Controller
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // ===== TRANG "ƯU ĐÃI CỦA TÔI" =====
    @GetMapping("/promotions")
    public String myPromotions(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Double totalSpent = promotionService.getTotalSpent(user.getId());

        List<Promotion> promotions = promotionService.findActive();

        promotions.sort(Comparator.comparing(
                p -> p.getMinTotalSpent() == null ? 0.0 : p.getMinTotalSpent()));

        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("promotions", promotions);
        model.addAttribute("content", "user/promotions");

        return "layout/main";
    }
}