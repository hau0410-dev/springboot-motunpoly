package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.User;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // ===== DASHBOARD SHIPPER =====
    @GetMapping("/index")
    public String index(Model model,
                        HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if(user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepo.findAll();

        model.addAttribute("orders", orders);

        return "shipper/index";
    }

    // ===== CHI TIẾT ĐƠN =====
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id,
                         Model model) {

        Order order =
                orderRepo.findById(id).orElse(null);

        model.addAttribute("order", order);

        model.addAttribute("items",
                orderItemRepo.findByOrderId(id));

        return "shipper/detail";
    }

    // ===== ĐỔI TRẠNG THÁI =====
    @GetMapping("/delivered/{id}")
    public String delivered(@PathVariable("id") Integer id) {

        Order order =
                orderRepo.findById(id).orElse(null);

        if(order != null) {

            // ĐANG GIAO -> ĐÃ GIAO
            if("DANG_GIAO".equals(order.getStatus())) {

                order.setStatus("DA_GIAO");

            }

            // ĐÃ GIAO -> ĐANG GIAO
            else if("DA_GIAO".equals(order.getStatus())) {

                order.setStatus("DANG_GIAO");

            }

            orderRepo.save(order);
        }

        return "redirect:/shipper/index";
    }
}