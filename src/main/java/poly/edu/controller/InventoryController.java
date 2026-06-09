package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Product;
import poly.edu.service.ProductService;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    ProductService productService;

    @GetMapping
    public String inventory(Model model) {

        model.addAttribute(
                "products",
                productService.findAll());

        return "admin/inventory/index";
    }

    @PostMapping("/update")
    public String updateStock(
            @RequestParam("id") Integer id,
            @RequestParam("stock") Integer stock) {

        Product product =
                productService.findById(id);

        if(product != null){

            product.setStock(stock);

            productService.save(product);
        }

        return "redirect:/admin/inventory";
    }
}