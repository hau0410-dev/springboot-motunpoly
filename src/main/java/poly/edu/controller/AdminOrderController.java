package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderItemRespository;
import poly.edu.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    @Autowired
    private OrderService orderService;

    // ===== DANH SÁCH ĐƠN HÀNG =====
    @GetMapping
    public String list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "status", required = false) String status,
            Model model) {

        List<Order> orders = orderService.search(name, status);

        model.addAttribute("orders", orders);
        model.addAttribute("name", name);
        model.addAttribute("status", status);

        return "admin/orders";
    }

    // ===== CHI TIẾT ĐƠN HÀNG =====
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Integer id, Model model) {

        Order order = orderService.findById(id);
        List<OrderItem> items = orderItemRepo.findByOrderId(id);

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "admin/order-detail";
    }

    @GetMapping("/update/{id}")
    public String updateStatus(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);

        if (order != null) {
            if ("ĐANG_GIAO".equals(order.getStatus())) {
                order.setStatus("DA_GIAO");
            } else {
                order.setStatus("ĐANG_GIAO");
            }
            orderService.save(order);
        }

        return "redirect:/admin/orders";
    }

}
