package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Product;
import poly.edu.service.ProductService;

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
    public ResponseEntity<Product> getById(@PathVariable Integer id) {
        Product p = productService.findById(id);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(p);
    }

    // ===== 3. CREATE =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {

        if (product.getActive() == null) {
            product.setActive(true);
        }
        if (product.getStock() == null) {
            product.setStock(0); // FIX: cột stock trong DB là NOT NULL -> tránh insert lỗi nếu Postman quên gửi
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(product));
        } catch (DataIntegrityViolationException e) {
            // FIX: category_id không tồn tại (FK fk_products_categories) sẽ rơi vào đây
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể tạo sản phẩm: category_id không tồn tại hoặc thiếu field bắt buộc (stock...).");
        }
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Product product) {

        Product old = productService.findById(id);

        if (old == null) {
            return ResponseEntity.notFound().build();
        }

        product.setId(id);

        if (product.getActive() == null) {
            product.setActive(old.getActive());
        }
        if (product.getStock() == null) {
            product.setStock(old.getStock()); // FIX: tránh set NULL vào cột NOT NULL "stock"
        }

        try {
            return ResponseEntity.ok(productService.save(product));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Không thể cập nhật sản phẩm: category_id không tồn tại hoặc dữ liệu không hợp lệ.");
        }
    }

    // ===== 5. DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {

        if (productService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            productService.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (DataIntegrityViolationException e) {
            // FIX: sản phẩm đang được order_items / reviews tham chiếu (FK không CASCADE)
            // -> trả 409 rõ ràng thay vì HTML lỗi 500. (product_images thì CASCADE nên không lỗi ở bảng đó)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xoá sản phẩm id=" + id + ": sản phẩm đang có trong đơn hàng hoặc đánh giá.");
        }
    }

    // ===== 6. SEARCH ===== (theo name)
    @GetMapping("/search")
    public List<Product> search(@RequestParam String keyword) {
        return productService.search(keyword);
    }

    // ===== 7. FILTER =====
    // FIX: Pageable KHÔNG được gắn @RequestParam - Spring tự bind từ query param page/size/sort
    // (ví dụ: /api/admin/products/filter?page=0&size=10&sort=price,asc). Gắn @RequestParam vào
    // Pageable là sai kiểu, Spring không khởi tạo được -> lỗi 400 mỗi lần gọi.
    // FIX: trước đây brand/vehicleType/partsBrand bị truyền nhầm 3 lần bằng "keyword" -> không lọc
    // được theo hãng xe/loại xe/hãng phụ tùng. Giờ nhận đúng 3 query param riêng biệt.
    @GetMapping("/filter")
    public Page<Product> filter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String partsBrand,
            Pageable pageable) {

        return productService.filter(keyword, categoryId, min, max, brand, vehicleType, partsBrand, pageable);
    }

    // ===== 8. SUGGEST =====
    @GetMapping("/suggest")
    public List<Product> suggest() {
        return productService.getSuggestProducts();
    }
}