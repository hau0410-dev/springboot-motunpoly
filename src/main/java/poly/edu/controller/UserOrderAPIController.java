package poly.edu.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Payment;
import poly.edu.entity.User;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.PaymentService;

@RestController
@RequestMapping("/api/orders")
public class UserOrderAPIController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    @Autowired
    private PaymentService paymentService;

    // ===== POLLING TRẠNG THÁI THANH TOÁN (dùng ở trang chờ quét QR) =====
    @GetMapping("/{id}/payment-status")
    public Map<String, Object> paymentStatus(@PathVariable("id") Integer id, HttpSession session) {

        Map<String, Object> data = new HashMap<>();

        User user = (User) session.getAttribute("user");
        Order order = orderRepo.findById(id).orElse(null);

        // Chặn hỏi trạng thái thanh toán của đơn hàng người khác (kiểm tra quyền sở hữu)
        if (user == null || order == null || order.getUser() == null
                || !order.getUser().getId().equals(user.getId())) {
            data.put("status", "KHONG_TIM_THAY");
            return data;
        }

        Payment payment = paymentService.findByOrderId(id);

        if (payment == null) {
            data.put("status", "KHONG_TIM_THAY");
            return data;
        }

        data.put("status", payment.getPaymentStatus());
        return data;
    }

    // ===== 1. DANH SÁCH ĐƠN HÀNG =====
    @GetMapping
    public List<Order> list() {
        return orderRepo.findAll();
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