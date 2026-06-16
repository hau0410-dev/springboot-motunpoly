package poly.edu.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Product;
import poly.edu.entity.Promotion;
import poly.edu.entity.User;
import poly.edu.service.ProductService;
import poly.edu.service.PromotionService;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    @Autowired
    PromotionService promotionService;

    @Autowired
    ProductService productService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    // ===== DANH SÁCH + FORM THÊM MỚI =====
    @GetMapping
    public String index(Model model, HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("promotion", new Promotion());
        model.addAttribute("promotions", promotionService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("selectedIds", new ArrayList<Integer>());

        return "admin/promotions";
    }

    // ===== SỬA =====
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        Promotion promotion = promotionService.findById(id);

        List<Integer> selectedIds = new ArrayList<>();
        if (promotion != null && promotion.getProducts() != null) {
            for (Product p : promotion.getProducts()) {
                selectedIds.add(p.getId());
            }
        }

        model.addAttribute("promotion", promotion);
        model.addAttribute("promotions", promotionService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("selectedIds", selectedIds);

        return "admin/promotions";
    }

    // ===== LƯU (THÊM / SỬA) =====
    @PostMapping("/save")
    public String save(@ModelAttribute Promotion promotion,
                       @RequestParam(value = "productIds", required = false) List<Integer> productIds,
                       HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        if (promotion.getActive() == null) {
            promotion.setActive(true);
        }

        List<Product> products = new ArrayList<>();

        if (productIds != null) {
            for (Integer pid : productIds) {
                Product p = productService.findById(pid);
                if (p != null) {
                    products.add(p);
                }
            }
        }

        promotion.setProducts(products);

        promotionService.save(promotion);

        return "redirect:/admin/promotions";
    }

    // ===== XOÁ =====
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        promotionService.deleteById(id);

        return "redirect:/admin/promotions";
    }
}