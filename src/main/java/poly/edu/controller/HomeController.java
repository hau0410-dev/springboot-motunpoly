package poly.edu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Product;
import poly.edu.entity.Promotion;
import poly.edu.entity.User;
import poly.edu.repository.ProductImageRepository;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;
import poly.edu.service.PromotionService;

@Controller
public class HomeController {

    private CategoryService categoryService;
    private ProductService productService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    public HomeController(CategoryService categoryService,
                          ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) Integer categoryId,
            @RequestParam(name = "min", required = false) Double min,
            @RequestParam(name = "max", required = false) Double max,
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpSession session,
            Model model) {

        model.addAttribute("categories",
                categoryService.getActiveCategories());

        Pageable pageable = PageRequest.of(page, 8); // 8 sản phẩm / trang

        Page<Product> productPage;

        if ((keyword != null && !keyword.isEmpty()) ||
            categoryId != null ||
            min != null ||
            max != null) {

            productPage = productService.filter(keyword, categoryId, min, max, pageable);

            model.addAttribute("title", "Kết quả lọc");
            model.addAttribute("isSearch", true);

        } else {

            productPage = productService.getSuggestProducts(pageable);

            model.addAttribute("title", "MotunPoLy - Phụ tùng xe máy");
            model.addAttribute("isSearch", false);
        }

        model.addAttribute("products", productPage.getContent());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "home/index");

        // ===== KHUYẾN MÃI THEO KHÁCH HÀNG (cá nhân hoá, cần đăng nhập) =====
        model.addAttribute("promoMap", buildPromoMap(productPage.getContent(), session));

        // ===== KHU "ƯU ĐÃI NỔI BẬT" - hiện cho mọi người, không cần đăng nhập =====
        model.addAttribute("allPromotions", promotionService.findActive());

        return "layout/main";
    }

    @GetMapping("/product/detail") 
    public String productDetail(@RequestParam("id") Integer id, HttpSession session, Model model) { 
    	Product product = productService.findById(id);
    	model.addAttribute("product", product); 
    	model.addAttribute("images", productImageRepository.findByProduct_IdOrderBySortOrderAsc(id));

    	// lượt thích "ảo" - cố định theo id sản phẩm để không bị nhảy số khi reload
    	int likeCount = 50 + (id * 37) % 250;
    	model.addAttribute("likeCount", likeCount);

    	// ===== KHUYẾN MÃI ÁP DỤNG CHO SẢN PHẨM NÀY =====
    	User user = (User) session.getAttribute("user");
    	Integer userId = (user != null) ? user.getId() : null;

    	List<Promotion> eligible = promotionService.getEligiblePromotions(userId);
    	Promotion promo = promotionService.getPromotionForProduct(id, eligible);

    	model.addAttribute("promo", promo);

    	if (promo != null && product != null && product.getPrice() != null) {
    		model.addAttribute("finalPrice", calcFinalPrice(product.getPrice(), promo));
    	}

    	model.addAttribute("content", "product/detail"); 
    	return "layout/main"; }

    // ===== TÍNH MAP: productId -> khuyến mãi đang áp dụng (cho danh sách sản phẩm) =====
    private Map<Integer, Promotion> buildPromoMap(List<Product> products, HttpSession session) {

        Map<Integer, Promotion> map = new HashMap<>();

        User user = (User) session.getAttribute("user");
        Integer userId = (user != null) ? user.getId() : null;

        List<Promotion> eligible = promotionService.getEligiblePromotions(userId);

        if (eligible.isEmpty()) {
            return map;
        }

        for (Product p : products) {
            Promotion promo = promotionService.getPromotionForProduct(p.getId(), eligible);
            if (promo != null) {
                map.put(p.getId(), promo);
            }
        }

        return map;
    }

    // ===== TÍNH GIÁ SAU KHI ÁP DỤNG KHUYẾN MÃI =====
    private Double calcFinalPrice(Double price, Promotion promo) {

        if (promo.getDiscountType() == null) {
            return price;
        }

        switch (promo.getDiscountType()) {

            case "PERCENT":
                double percent = (promo.getDiscountValue() == null) ? 0 : promo.getDiscountValue();
                return price - (price * percent / 100);

            case "AMOUNT":
                double amount = (promo.getDiscountValue() == null) ? 0 : promo.getDiscountValue();
                double result = price - amount;
                return Math.max(result, 0);

            default:
                // GIFT hoặc các loại khác -> không thay đổi giá
                return price;
        }
    }

}