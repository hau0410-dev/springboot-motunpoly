package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderAPIController {
	
	@Autowired
	private OrderRepository orderRepo;
	
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // ===== DANH SÁCH =====
    @GetMapping
    public List<Order> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "status", required = false) String status) {

        return orderService.search(name, status);
    }

    // ===== CHI TIẾT =====
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id") Integer id) {
        return orderService.findById(id);
    }

    // ===== LẤY ITEM =====
    @GetMapping("/{id}/items")
    public List<OrderItem> getItems(@PathVariable("id") Integer id) {
        return orderItemRepo.findByOrderId(id);
    }

    // ===== UPDATE STATUS =====
    @PutMapping("/update/{id}")
    public Order updateStatus(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);

        if (order != null) {
            if ("ĐANG_GIAO".equals(order.getStatus())) {
                order.setStatus("DA_GIAO");
            } else {
                order.setStatus("ĐANG_GIAO");
            }

            orderService.save(order); // void
        }

        return order; // trả lại object sau khi update
    }

    // ===== CREATE =====
    @PostMapping
    public Order create(@RequestBody Order order) {
        orderService.save(order); // void
        return order; // trả lại object đã lưu
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Integer id) {
        orderRepo.deleteById(id); // gọi thẳng repo
        return "Deleted successfully";
    }
}