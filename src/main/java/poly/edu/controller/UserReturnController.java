package poly.edu.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.ReturnOrder;
import poly.edu.entity.User;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ReturnOrderRepository;

@Controller
@RequestMapping("/returns")
public class UserReturnController {

    // Số ngày tối đa được yêu cầu hoàn hàng, kể từ lúc khách xác nhận đã nhận hàng
    private static final int RETURN_WINDOW_DAYS = 3;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    @Autowired
    private ReturnOrderRepository returnOrderRepo;

    // ===== DANH SÁCH ĐƠN HOÀN CỦA TÔI =====
    @GetMapping
    public String list(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<ReturnOrder> returns = returnOrderRepo.findByUser_IdOrderByIdDesc(user.getId());

        model.addAttribute("returns", returns);

        return "user/return-list";
    }

    // ===== FORM TẠO YÊU CẦU HOÀN HÀNG =====
    @GetMapping("/new/{orderId}")
    public String newReturnForm(@PathVariable("orderId") Integer orderId, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        String errorMsg = checkEligible(order, user);

        if (errorMsg != null) {
            model.addAttribute("errorMsg", errorMsg);
            return "user/return-new";
        }

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "user/return-new";
    }

    // ===== TẠO YÊU CẦU HOÀN HÀNG =====
    @PostMapping("/create/{orderId}")
    public String create(
            @PathVariable("orderId") Integer orderId,
            @RequestParam("reason") String reason,
            @RequestParam(value = "reasonNote", required = false) String reasonNote,
            HttpSession session,
            RedirectAttributes redirect) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        String errorMsg = checkEligible(order, user);

        if (errorMsg != null) {
            redirect.addFlashAttribute("errorMsg", errorMsg);
            return "redirect:/orders/" + orderId;
        }

        ReturnOrder ro = new ReturnOrder();
        ro.setOrder(order);
        ro.setUser(user);
        ro.setReason(reason);
        ro.setReasonNote(reasonNote);
        ro.setRefundAmount(order.getTotalAmount());
        ro.setStatus("CHO_XAC_NHAN");
        ro.setRequestedDate(LocalDateTime.now());

        returnOrderRepo.save(ro);

        redirect.addFlashAttribute("message", "Đã gửi yêu cầu hoàn hàng, vui lòng chờ người bán xác nhận.");

        return "redirect:/returns/" + ro.getId();
    }

    // ===== CHI TIẾT ĐƠN HOÀN =====
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        ReturnOrder ro = returnOrderRepo.findById(id).orElse(null);

        // Chặn xem đơn hoàn của người khác
        if (ro == null || ro.getUser() == null || !ro.getUser().getId().equals(user.getId())) {
            return "redirect:/returns";
        }

        List<OrderItem> items = orderItemRepo.findByOrderId(ro.getOrder().getId());

        model.addAttribute("ro", ro);
        model.addAttribute("items", items);

        return "user/return-detail";
    }

    // ===== KIỂM TRA ĐIỀU KIỆN ĐƯỢC PHÉP YÊU CẦU HOÀN HÀNG =====
    // Trả về null nếu hợp lệ, hoặc thông báo lỗi nếu không hợp lệ
    private String checkEligible(Order order, User user) {

        if (order == null || order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            return "Không tìm thấy đơn hàng.";
        }

        if (!"HOAN_THANH".equals(order.getStatus())) {
            return "Đơn hàng chưa hoàn tất nên không thể yêu cầu hoàn hàng.";
        }

        if (order.getCompletedDate() == null) {
            return "Không xác định được ngày nhận hàng của đơn này.";
        }

        LocalDateTime deadline = order.getCompletedDate().plusDays(RETURN_WINDOW_DAYS);

        if (LocalDateTime.now().isAfter(deadline)) {
            return "Đã quá hạn " + RETURN_WINDOW_DAYS + " ngày được yêu cầu hoàn hàng kể từ khi nhận hàng.";
        }

        ReturnOrder existing = returnOrderRepo.findByOrder_Id(order.getId());

        if (existing != null) {
            return "Đơn hàng này đã có yêu cầu hoàn hàng trước đó.";
        }

        return null;
    }
}