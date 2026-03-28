package poly.edu.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;

@RestController
@RequestMapping("/api/orders")
public class UserOrderAPIController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // ===== 1. DANH SÁCH ĐƠN HÀNG =====
    @GetMapping
    public List<Order> list() {
        return orderRepo.findAll(); // giữ nguyên logic
    }

    // ===== 2. CHI TIẾT ĐƠN HÀNG =====
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable("id") Integer id) {

        Order order = orderRepo.findById(id).orElse(null);
        List<OrderItem> items = orderItemRepo.findByOrderId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);

        return data;
    }
}