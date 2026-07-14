package poly.edu.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.OrderItem;
import poly.edu.entity.ReturnOrder;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.ReturnOrderRepository;

@Controller
@RequestMapping("/admin/returns")
public class AdminReturnController {

    @Autowired
    private ReturnOrderRepository returnOrderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // ===== DANH SÁCH ĐƠN HOÀN =====
    @GetMapping
    public String list(
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        List<ReturnOrder> returns = (status == null || status.isEmpty())
                ? returnOrderRepo.findAllByOrderByIdDesc()
                : returnOrderRepo.findByStatusOrderByIdDesc(status);

        model.addAttribute("returns", returns);
        model.addAttribute("status", status);

        return "admin/returns/list";
    }

    // ===== CHI TIẾT ĐƠN HOÀN =====
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {

        ReturnOrder ro = returnOrderRepo.findById(id).orElse(null);

        List<OrderItem> items = (ro != null)
                ? orderItemRepo.findByOrderId(ro.getOrder().getId())
                : List.of();

        model.addAttribute("ro", ro);
        model.addAttribute("items", items);

        return "admin/returns/detail";
    }

    // ===== ADMIN DUYỆT ĐƠN HOÀN: CHO_XAC_NHAN -> DA_XAC_NHAN =====
    @GetMapping("/confirm/{id}")
    public String confirm(@PathVariable("id") Integer id) {

        ReturnOrder ro = returnOrderRepo.findById(id).orElse(null);

        if (ro != null && "CHO_XAC_NHAN".equals(ro.getStatus())) {
            ro.setStatus("DA_XAC_NHAN");
            ro.setConfirmedDate(LocalDateTime.now());
            returnOrderRepo.save(ro);
        }

        return "redirect:/admin/returns/" + id;
    }

    // ===== ADMIN TỪ CHỐI ĐƠN HOÀN: CHO_XAC_NHAN -> DA_TU_CHOI =====
    @PostMapping("/reject/{id}")
    public String reject(
            @PathVariable("id") Integer id,
            @RequestParam("adminNote") String adminNote) {

        ReturnOrder ro = returnOrderRepo.findById(id).orElse(null);

        if (ro != null && "CHO_XAC_NHAN".equals(ro.getStatus())) {
            ro.setStatus("DA_TU_CHOI");
            ro.setAdminNote(adminNote);
            ro.setRejectedDate(LocalDateTime.now());
            returnOrderRepo.save(ro);
        }

        return "redirect:/admin/returns/" + id;
    }
}