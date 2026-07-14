package poly.edu.controller;

import poly.edu.entity.*;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductImageRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;


import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")	
public class AdminController {

    @Autowired
    ProductService productService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    UserRepository userRepo;

    @Value("${app.upload.dir:uploads/imges}")
    private String uploadDir;

    
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
    @GetMapping("")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("totalProducts", productService.findAll().size());
        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalOrders", orderRepo.count());

        double revenue = orderRepo.findAll().stream()
            .filter(o -> "HOAN_THANH".equals(o.getStatus()) || "DA_GIAO".equals(o.getStatus()))
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0)
            .sum();

        model.addAttribute("totalRevenue", revenue);

        // ===== ĐƠN HÀNG MỚI (CHỜ XÁC NHẬN) - HIỂN THỊ BANNER THÔNG BÁO =====
        List<Order> newOrders = orderRepo.findByStatus("CHO_XAC_NHAN");
        model.addAttribute("newOrders", newOrders);

        return "admin/index";
    }
    


    // ================= PRODUCTS =================

    @GetMapping({"/products", "/products/"})
    public String list(Model model, HttpSession session) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/index";
    }
    


    @GetMapping("/products/create")
    public String createForm(Model model, HttpSession session) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("images", new java.util.ArrayList<ProductImage>());
        return "admin/products/form";
    }


    @PostMapping("/products/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam("imageFile") MultipartFile imageFile,
                       @RequestParam(value = "galleryFiles", required = false) List<MultipartFile> galleryFiles,
                       HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        
        Product oldProduct = null;

        if (product.getId() != null) {
            oldProduct = productService.findById(product.getId());
            if (oldProduct != null && product.getActive() == null) {
                product.setActive(oldProduct.getActive());
            }
        }

        // ===== UPLOAD ẢNH ĐẠI DIỆN =====
        if (!imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();

                Path uploadDirPath = Paths.get(uploadDir);
                Files.createDirectories(uploadDirPath);

                Path filePath = uploadDirPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (oldProduct != null) {
            // Không chọn ảnh mới -> giữ nguyên ảnh đại diện cũ
            product.setImage(oldProduct.getImage());
        }

        
        if (product.getActive() == null) {
            product.setActive(true);
        }

        Product saved = productService.save(product);

        // ===== UPLOAD ẢNH GALLERY (3-4 ảnh phụ) =====
        if (galleryFiles != null) {

            try {
                Path uploadDirPath = Paths.get(uploadDir);
                Files.createDirectories(uploadDirPath);

                // lấy sortOrder lớn nhất hiện có để các ảnh mới nối tiếp phía sau
                List<ProductImage> existedImages =
                        productImageRepository.findByProduct_IdOrderBySortOrderAsc(saved.getId());

                int nextOrder = existedImages.size();

                for (MultipartFile file : galleryFiles) {

                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                    Path filePath = uploadDirPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    ProductImage pi = new ProductImage();
                    pi.setProduct(saved);
                    pi.setImageUrl(fileName);
                    pi.setSortOrder(nextOrder++);

                    productImageRepository.save(pi);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/admin/products";
    }




    
    @RequestMapping("/products/update")
    public String update(@ModelAttribute Product product,
            HttpSession session) {
        productService.save(product);
        return "redirect:/category/edit/" ;
    }

    @GetMapping("/products/edit/{id}")
    public String edit(@PathVariable("id") Integer id,
                       Model model,
                       HttpSession session) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("images", productImageRepository.findByProduct_IdOrderBySortOrderAsc(id));
        return "admin/products/form";
    }

    // ===== XOÁ 1 ẢNH TRONG GALLERY =====
    @GetMapping("/products/image/delete/{id}")
    public String deleteImage(@PathVariable("id") Integer id, HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        ProductImage image = productImageRepository.findById(id).orElse(null);

        if (image != null) {
            Integer productId = image.getProduct().getId();
            productImageRepository.deleteById(id);
            return "redirect:/admin/products/edit/" + productId;
        }

        return "redirect:/admin/products";
    }


    
    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         HttpSession session) {     
        productService.deleteById(id);
        return "redirect:/admin/products";
    }
    @GetMapping("/product/detail/{id}")
    public String detail(
            @PathVariable Integer id,
            Model model,
            HttpSession session){

        Product product =
                productService.findById(id);

        model.addAttribute("product", product);
        model.addAttribute("images", productImageRepository.findByProduct_IdOrderBySortOrderAsc(id));
        model.addAttribute("likeCount", 50 + (id * 37) % 250);

        String stockMessage =
                (String) session.getAttribute("stockMessage");

        model.addAttribute(
                "stockMessage",
                stockMessage
        );

        session.removeAttribute("stockMessage");

        return "product/detail";
    }


}