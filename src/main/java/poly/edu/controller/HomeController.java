package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import poly.edu.entity.Product;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;

@Controller
public class HomeController {

    private CategoryService categoryService;
    private ProductService productService;

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

        return "layout/main";
    }
    @GetMapping("/product/detail") 
    public String productDetail(@RequestParam("id") Integer id, Model model) { 
    	model.addAttribute("product", productService.findById(id)); 
    	model.addAttribute("content", "product/detail"); 
    	return "layout/main"; }
   
}
