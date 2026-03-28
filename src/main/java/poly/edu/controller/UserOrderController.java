package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;

@Controller
@RequestMapping("/orders")
public class UserOrderController {

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    OrderItemRespository orderItemRepo;

    // ===== DANH SÁCH ĐƠN HÀNG =====
    @GetMapping
    public String list(Model model) {
        List<Order> orders = orderRepo.findAll(); // tạm thời lấy tất cả
        model.addAttribute("orders", orders);
        return "user/orders";
    }

    // ===== CHI TIẾT ĐƠN HÀNG =====
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {

        Order order = orderRepo.findById(id).orElse(null);
        List<OrderItem> items = orderItemRepo.findByOrderId(id);

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "user/order-detail";
    }
}