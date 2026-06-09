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

    @GetMapping("/update/{id}")
    public String updateStatus(@PathVariable("id") Integer id) {

        Order order = orderService.findById(id);
        
        Payment payment =
                paymentService.findByOrderId(id);

        if(payment != null){

            if("CHO_THANH_TOAN".equals(payment.getPaymentStatus())){

                payment.setPaymentStatus("DA_THANH_TOAN");

            }else{

                payment.setPaymentStatus("CHO_THANH_TOAN");

            }

            paymentService.save(payment);
        }
        
        if (order != null) {

            // CHỜ XÁC NHẬN -> ĐANG GIAO
            if ("CHO_XAC_NHAN".equals(order.getStatus())) {

                order.setStatus("DANG_GIAO");

            }

            // ĐANG GIAO -> ĐÃ GIAO
            else if ("DANG_GIAO".equals(order.getStatus())) {

                order.setStatus("DA_GIAO");

            }

            // ĐÃ GIAO -> CHỜ XÁC NHẬN
            else if ("DA_GIAO".equals(order.getStatus())) {

                order.setStatus("CHO_XAC_NHAN");

            }

            orderService.save(order);
        }

        return "redirect:/admin/orders";
    }
    @GetMapping("/confirm-payment/{id}")
    public String confirmPayment(
            @PathVariable("id") Integer id) {

        Payment payment = paymentService.findByOrderId(id);

        if (payment != null) {
            payment.setPaymentStatus("DA_THANH_TOAN");
            paymentService.save(payment);
        }

        return "redirect:/admin/orders/" + id;
    }

}
