package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Category;
import poly.edu.service.CategoryService;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAPIController {

    @Autowired
    private CategoryService categoryService;

    // ===== 1. DANH SÁCH =====
    @GetMapping
    public List<Category> list() {
        return categoryService.findByType("PRODUCT");
    }

    // ===== 2. CHI TIẾT =====
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        Category c = categoryService.findById(id);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(c);
    }

    // ===== 3. CREATE =====
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getType() == null || category.getType().isBlank()) {
            category.setType("PRODUCT"); // FIX: cột "type" NOT NULL trong DB
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.save(category));
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id,
                           @RequestBody Category category) {

        Category old = categoryService.findById(id);

        if (old == null) {
            return ResponseEntity.notFound().build();
        }

        category.setId(id);

        if (category.getIsActive() == null) {
            category.setIsActive(old.getIsActive());
        }
        if (category.getType() == null || category.getType().isBlank()) {
            category.setType(old.getType());
        }

        return ResponseEntity.ok(categoryService.save(category));
    }

    // ===== 5. DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

        if (categoryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            categoryService.delete(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (DataIntegrityViolationException e) {
            // FIX: category đang có Products.category_id trỏ tới (FK không CASCADE) -> chặn xoá,
            // trả 409 rõ nghĩa thay vì lỗi 500.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xoá danh mục id=" + id + ": đang có sản phẩm thuộc danh mục này.");
        }
    }
}