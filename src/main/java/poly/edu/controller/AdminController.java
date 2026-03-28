package poly.edu.controller;

import poly.edu.entity.*;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
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

    
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
    @GetMapping("")
    public String dashboard(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
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
        return "admin/products/form";
    }


    @PostMapping("/products/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam("imageFile") MultipartFile imageFile,
                       HttpSession session) {

        if (!isAdmin(session)) return "redirect:/login";

        
        if (product.getId() != null) {
            Product oldProduct = productService.findById(product.getId());
            if (oldProduct != null && product.getActive() == null) {
                product.setActive(oldProduct.getActive());
            }
        }

        // ===== UPLOAD ẢNH =====
        if (!imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();

                Path uploadDir = Paths.get("src/main/resources/static/imges");
                Files.createDirectories(uploadDir);

                Path filePath = uploadDir.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
        if (product.getActive() == null) {
            product.setActive(true);
        }

        productService.save(product);

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
        return "admin/products/form";
    }


    
    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         HttpSession session) {     
        productService.deleteById(id);
        return "redirect:/admin/products";
    }


}

