package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.Payment;
import poly.edu.entity.User;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.PaymentService;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    @Autowired
    private PaymentService paymentService;

    // ===== KIỂM TRA QUYỀN SHIPPER =====
    private User requireShipper(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"SHIPPER".equalsIgnoreCase(user.getRole())) {
            return null;
        }

        return user;
    }

    // ===== DASHBOARD SHIPPER =====
    @GetMapping("/index")
    public String index(Model model, HttpSession session) {

        User shipper = requireShipper(session);

        if (shipper == null) {
            return "redirect:/login";
        }

        // đơn đã được admin xác nhận, đang chờ shipper lấy hàng (chưa ai nhận)
        List<Order> waitingOrders = orderRepo.findByStatus("CHO_LAY_HANG");

        // đơn mà chính shipper này đang giao
        List<Order> myOrders = orderRepo.findByStatusAndShipper_Id("DANG_GIAO", shipper.getId());

        model.addAttribute("shipper", shipper);
        model.addAttribute("waitingOrders", waitingOrders);
        model.addAttribute("myOrders", myOrders);

        return "shipper/index";
    }

    // ===== CHI TIẾT ĐƠN =====
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model, HttpSession session) {

        User shipper = requireShipper(session);

        if (shipper == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(id).orElse(null);

        model.addAttribute("order", order);
        model.addAttribute("items", orderItemRepo.findByOrderId(id));
        model.addAttribute("shipper", shipper);

        return "shipper/detail";
    }

    // ===== LỊCH SỬ GIAO HÀNG =====
    @GetMapping("/history")
    public String history(
            @RequestParam(value = "filter", required = false, defaultValue = "week") String filter,
            Model model, HttpSession session) {

        User shipper = requireShipper(session);
        if (shipper == null) return "redirect:/login";

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime from;

        switch (filter) {
            case "day":
                from = now.toLocalDate().atStartOfDay();
                break;
            case "month":
                from = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            default: // week
                from = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        }

        List<Order> history = orderRepo.findShipperHistory(shipper.getId(), from, null);

        // thống kê nhanh
        long totalDelivered = history.stream()
            .filter(o -> "HOAN_THANH".equals(o.getStatus()) || "DA_GIAO".equals(o.getStatus()))
            .count();

        long totalRisk = history.stream()
            .filter(o -> "KHIEU_NAI".equals(o.getStatus()))
            .count();

        double totalRevenue = history.stream()
            .filter(o -> "HOAN_THANH".equals(o.getStatus()) || "DA_GIAO".equals(o.getStatus()))
            .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0)
            .sum();

        model.addAttribute("shipper", shipper);
        model.addAttribute("history", history);
        model.addAttribute("filter", filter);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalRisk", totalRisk);
        model.addAttribute("totalRevenue", totalRevenue);

        return "shipper/history";
    }

    // ===== SHIPPER NHẬN LẤY HÀNG: CHO_LAY_HANG -> DANG_GIAO =====
    @GetMapping("/pickup/{id}")
    public String pickup(@PathVariable("id") Integer id, HttpSession session) {

        User shipper = requireShipper(session);

        if (shipper == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(id).orElse(null);

        if (order != null && "CHO_LAY_HANG".equals(order.getStatus())) {
            order.setStatus("DANG_GIAO");
            order.setShipper(shipper);
            orderRepo.save(order);
        }

        return "redirect:/shipper/index";
    }

    // ===== SHIPPER GIAO THÀNH CÔNG: DANG_GIAO -> DA_GIAO =====
    @GetMapping("/delivered/{id}")
    public String delivered(@PathVariable("id") Integer id, HttpSession session) {

        User shipper = requireShipper(session);

        if (shipper == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(id).orElse(null);

        if (order != null
                && "DANG_GIAO".equals(order.getStatus())
                && order.getShipper() != null
                && order.getShipper().getId().equals(shipper.getId())) {

            order.setStatus("DA_GIAO");
            orderRepo.save(order);

            // COD: coi như đã thu tiền khi giao thành công
            Payment payment = paymentService.findByOrderId(order.getId());

            if (payment != null && "COD".equals(payment.getPaymentMethod())) {
                payment.setPaymentStatus("DA_THANH_TOAN");
                paymentService.save(payment);
            }
        }

        return "redirect:/shipper/index";
    }
}