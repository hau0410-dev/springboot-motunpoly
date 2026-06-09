package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Product;
import poly.edu.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductAPIController {

    @Autowired
    private ProductService productService;

    // ===== 1. DANH SÁCH =====
    @GetMapping
    public List<Product> list() {
        return productService.findAll();
    }

    // ===== 2. CHI TIẾT =====
    @GetMapping("/{id}")
    public Product getById(@PathVariable Integer id) {
        return productService.findById(id);
    }

    // ===== 3. CREATE =====
    @PostMapping
    public Product create(@RequestBody Product product) {

        if (product.getActive() == null) {
            product.setActive(true);
        }

        return productService.save(product);
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id, @RequestBody Product product) {

        Product old = productService.findById(id);

        if (old != null) {
            product.setId(id);

            if (product.getActive() == null) {
                product.setActive(old.getActive());
            }

            return productService.save(product);
        }

        return null;
    }

    // ===== 5. DELETE =====
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        productService.deleteById(id);
        return "Deleted successfully";
    }

    // ===== 6. SEARCH ===== (theo name)
    @GetMapping("/search")
    public List<Product> search(@RequestParam String keyword) {
        return productService.search(keyword);
    }

    // ===== 7. FILTER ===== (giống query repo của bạn)
    @GetMapping("/filter")
    public Page<Product> filter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max, 
    		@RequestParam(required = false) Pageable pageable) {
    			
        return productService.filter(keyword, categoryId, min, max, pageable );
    }

    // ===== 8. SUGGEST =====
    @GetMapping("/suggest")
    public List<Product> suggest() {
        return productService.getSuggestProducts();
    }
}