package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Payment;
import poly.edu.entity.User;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.*;

@Controller
@RequestMapping("/orders")
public class UserOrderController {
	@Autowired
	private PaymentService paymentService;
	
    @Autowired
    OrderRepository orderRepo;

    @Autowired
    OrderItemRespository orderItemRepo;
    
    @Autowired
    private OrderService orderService;

    // ===== DANH SÁCH ĐƠN HÀNG =====
    @GetMapping
    public String list(Model model,
                       HttpSession session) {

        User user =
            (User) session.getAttribute("user");

        if(user == null){
            return "redirect:/login";
        }

        List<Order> orders =
            orderService.findByUserId(user.getId());

        model.addAttribute("orders", orders);

        return "user/orders";
    }

    // ===== CHI TIẾT ĐƠN HÀNG =====
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {

        Order order = orderRepo.findById(id).orElse(null);
        List<OrderItem> items = orderItemRepo.findByOrderId(id);
        Payment payment = paymentService.findByOrderId(id);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("payment", payment);
        

        return "user/order-detail";
    }

    // ===== KHÁCH XÁC NHẬN ĐÃ NHẬN HÀNG: DA_GIAO -> HOAN_THANH =====
    @GetMapping("/confirm-received/{id}")
    public String confirmReceived(@PathVariable("id") Integer id, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(id).orElse(null);

        if (order != null
                && order.getUser() != null
                && order.getUser().getId().equals(user.getId())
                && "DA_GIAO".equals(order.getStatus())) {

            order.setStatus("HOAN_THANH");
            orderRepo.save(order);
        }

        return "redirect:/orders/" + id;
    }

    // ===== KHÁCH BÁO CHƯA NHẬN ĐƯỢC HÀNG: DA_GIAO -> KHIEU_NAI =====
    @GetMapping("/report-issue/{id}")
    public String reportIssue(@PathVariable("id") Integer id, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(id).orElse(null);

        if (order != null
                && order.getUser() != null
                && order.getUser().getId().equals(user.getId())
                && "DA_GIAO".equals(order.getStatus())) {

            order.setStatus("KHIEU_NAI");
            orderRepo.save(order);
        }

        return "redirect:/orders/" + id;
    }
}