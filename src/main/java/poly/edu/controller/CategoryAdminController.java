package poly.edu.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import poly.edu.entity.Category;
import poly.edu.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    @Autowired
    private CategoryService categoryService;

    // Thư mục lưu ảnh - dùng chung với ảnh sản phẩm (đã map ra URL /imges/**)
    @Value("${app.upload.dir:uploads/imges}")
    private String uploadDir;

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

    // Lưu (thêm mới / cập nhật)
    @PostMapping("/save")
    public String save(Category category,
                        @RequestParam(value = "iconFile", required = false) MultipartFile iconFile) {

        Category oldCategory = null;

        if (category.getId() != null) {
            oldCategory = categoryService.findById(category.getId());
            if (oldCategory != null && category.getIsActive() == null) {
                category.setIsActive(oldCategory.getIsActive());
            }
        }

        // ===== UPLOAD ẢNH ICON =====
        if (iconFile != null && !iconFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + iconFile.getOriginalFilename();

                Path uploadDirPath = Paths.get(uploadDir);
                Files.createDirectories(uploadDirPath);

                Path filePath = uploadDirPath.resolve(fileName);
                Files.copy(iconFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                category.setIcon(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (oldCategory != null) {
            // Không chọn ảnh mới -> giữ nguyên icon cũ
            category.setIcon(oldCategory.getIcon());
        }

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

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