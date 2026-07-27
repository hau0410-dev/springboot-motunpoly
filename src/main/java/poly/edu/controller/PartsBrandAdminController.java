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

// Quản lý "Hãng phụ tùng" (Brembo, NGK, Michelin...) - dùng chung bảng Categories,
// phân biệt bằng field type = "PARTS_BRAND"
@Controller
@RequestMapping("/admin/parts-brands")
public class PartsBrandAdminController {

    private static final String TYPE = "PARTS_BRAND";

    @Autowired
    private CategoryService categoryService;

    @Value("${app.upload.dir:uploads/imges}")
    private String uploadDir;

    // Hiển thị danh sách
    @GetMapping
    public String index(Model model) {

        model.addAttribute("partsBrand", new Category());
        model.addAttribute("partsBrands", categoryService.findByType(TYPE));

        return "admin/parts-brands";
    }

    // Click vào hãng phụ tùng để sửa
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("partsBrand", categoryService.findById(id));
        model.addAttribute("partsBrands", categoryService.findByType(TYPE));

        return "admin/parts-brands";
    }

    // Lưu (thêm mới / cập nhật)
    @PostMapping("/save")
    public String save(Category partsBrand,
                        @RequestParam(value = "iconFile", required = false) MultipartFile iconFile) {

        Category oldPartsBrand = null;

        if (partsBrand.getId() != null) {
            oldPartsBrand = categoryService.findById(partsBrand.getId());
            if (oldPartsBrand != null && partsBrand.getIsActive() == null) {
                partsBrand.setIsActive(oldPartsBrand.getIsActive());
            }
        }

        partsBrand.setType(TYPE);

        // ===== UPLOAD LOGO =====
        if (iconFile != null && !iconFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + iconFile.getOriginalFilename();

                Path uploadDirPath = Paths.get(uploadDir);
                Files.createDirectories(uploadDirPath);

                Path filePath = uploadDirPath.resolve(fileName);
                Files.copy(iconFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                partsBrand.setIcon(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (oldPartsBrand != null) {
            // Không chọn ảnh mới -> giữ nguyên logo cũ
            partsBrand.setIcon(oldPartsBrand.getIcon());
        }

        if (partsBrand.getIsActive() == null) {
            partsBrand.setIsActive(true);
        }

        categoryService.save(partsBrand);

        return "redirect:/admin/parts-brands";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        categoryService.delete(id);

        return "redirect:/admin/parts-brands";
    }
}