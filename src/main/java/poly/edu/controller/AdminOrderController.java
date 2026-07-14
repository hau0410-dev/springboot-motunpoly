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

import poly.edu.entity.Payment;
import poly.edu.service.PaymentService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
	
	@Autowired
	private PaymentService paymentService;
	
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

        Order order =
        		orderService.findById(id);

        List<OrderItem> items =
        		orderItemRepo.findByOrderId(id);

        Payment payment =
                paymentService.findByOrderId(id);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("payment", payment);

        return "admin/order-detail";

    }

    // ===== ADMIN XÁC NHẬN ĐƠN: CHO_XAC_NHAN -> CHO_LAY_HANG =====
    @GetMapping("/update/{id}")
    public String updateStatus(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);

        if (order != null && "CHO_XAC_NHAN".equals(order.getStatus())) {
            order.setStatus("CHO_LAY_HANG");
            orderService.save(order);
        }

        return "redirect:/admin/orders";
    }

    // ===== ADMIN XỬ LÝ KHIẾU NẠI: KHIEU_NAI -> CHO_LAY_HANG (giao lại) =====
    @GetMapping("/resolve/{id}")
    public String resolve(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);

        if (order != null && "KHIEU_NAI".equals(order.getStatus())) {
            order.setStatus("CHO_LAY_HANG");
            order.setShipper(null);
            orderService.save(order);
        }

        return "redirect:/admin/orders/" + id;
    }

    // ===== ADMIN XÁC NHẬN ĐÃ NHẬN TIỀN (BANKING: xác nhận CK / COD: xác nhận đã thu hộ) =====
    @GetMapping("/confirm-payment/{id}")
    public String confirmPayment(
            @PathVariable("id") Integer id) {

        Payment payment = paymentService.findByOrderId(id);

        if (payment != null) {
            payment.setPaymentStatus("THANH_CONG");
            paymentService.save(payment);
        }

        return "redirect:/admin/orders/" + id;
    }

}