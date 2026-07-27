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

// Quản lý "Hãng xe" (Honda, Yamaha, Suzuki...) - dùng chung bảng Categories,
// phân biệt bằng field type = "BRAND"
@Controller
@RequestMapping("/admin/brands")
public class BrandAdminController {

    private static final String TYPE = "BRAND";

    @Autowired
    private CategoryService categoryService;

    @Value("${app.upload.dir:uploads/imges}")
    private String uploadDir;

    // Hiển thị danh sách
    @GetMapping
    public String index(Model model) {

        model.addAttribute("brand", new Category());
        model.addAttribute("brands", categoryService.findByType(TYPE));

        return "admin/brands";
    }

    // Click vào hãng xe để sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("brand", categoryService.findById(id));
        model.addAttribute("brands", categoryService.findByType(TYPE));

        return "admin/brands";
    }

    // Lưu (thêm mới / cập nhật)
    @PostMapping("/save")
    public String save(Category brand,
                        @RequestParam(value = "iconFile", required = false) MultipartFile iconFile) {

        Category oldBrand = null;

        if (brand.getId() != null) {
            oldBrand = categoryService.findById(brand.getId());
            if (oldBrand != null && brand.getIsActive() == null) {
                brand.setIsActive(oldBrand.getIsActive());
            }
        }

        brand.setType(TYPE);

        // ===== UPLOAD LOGO =====
        if (iconFile != null && !iconFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + iconFile.getOriginalFilename();

                Path uploadDirPath = Paths.get(uploadDir);
                Files.createDirectories(uploadDirPath);

                Path filePath = uploadDirPath.resolve(fileName);
                Files.copy(iconFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                brand.setIcon(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (oldBrand != null) {
            // Không chọn ảnh mới -> giữ nguyên logo cũ
            brand.setIcon(oldBrand.getIcon());
        }

        if (brand.getIsActive() == null) {
            brand.setIsActive(true);
        }

        categoryService.save(brand);

        return "redirect:/admin/brands";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        categoryService.delete(id);

        return "redirect:/admin/brands";
    }
}