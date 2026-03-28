package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Category;
import poly.edu.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách
    @GetMapping
    public String index(Model model) {

        model.addAttribute("category", new Category());
        model.addAttribute("categories", categoryService.findAll());

        return "admin/categories";
    }

    // Click vào danh mục để sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("category",
                categoryService.findById(id));

        model.addAttribute("categories",
                categoryService.findAll());

        return "admin/categories";
    }

    // Lưu
    @PostMapping("/save")
    public String save(Category category) {

        categoryService.save(category);

        return "redirect:/admin/categories";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        categoryService.delete(id);

        return "redirect:/admin/categories";
    }
}