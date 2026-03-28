package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
        return categoryService.findAll();
    }

    // ===== 2. CHI TIẾT =====
    @GetMapping("/{id}")
    public Category getById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    // ===== 3. CREATE =====
    @PostMapping
    public Category create(@RequestBody Category category) {

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        return categoryService.save(category);
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public Category update(@PathVariable Integer id,
                           @RequestBody Category category) {

        Category old = categoryService.findById(id);

        if (old != null) {
            category.setId(id);

            if (category.getIsActive() == null) {
                category.setIsActive(old.getIsActive());
            }

            return categoryService.save(category);
        }

        return null;
    }

    // ===== 5. DELETE =====
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Integer id) {
        categoryService.delete(id);
        return "Deleted successfully";
    }

   
}