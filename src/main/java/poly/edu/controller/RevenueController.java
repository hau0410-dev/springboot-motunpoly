package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Revenue;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.RevenueRepository;
import java.text.DecimalFormat;
import java.util.List;

@Controller
public class RevenueController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    RevenueRepository revenueRepository;
    
    
    
    // Load trang revenue
    @GetMapping("/admin/revenue")
    public String revenue(Model model) {

        // Xóa dữ liệu cũ
        revenueRepository.deleteAll();

        // Lấy orders
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            // Chỉ tính đơn đã giao
            if ("DA_GIAO".equals(order.getStatus())) {

                for (OrderItem item : order.getOrderItems()) {

                    Revenue r = new Revenue();
                    r.setCustomerName(order.getFullname());
                    r.setProductName(item.getProduct().getName());
                    r.setPrice(item.getPrice());
                    r.setQuantity(item.getQuantity());
                    r.setSubtotal(item.getSubtotal());
                    r.setCreatedDate(order.getCreatedDate());

                    revenueRepository.save(r);
                }
            }
        }
        model.addAttribute("topProducts", revenueRepository.getTopProducts());
        
        model.addAttribute("chartLabels", revenueRepository.getChartDates());
        model.addAttribute("chartData", revenueRepository.getChartTotals());
        
        Double maxToday = revenueRepository.getMaxToday();
        model.addAttribute("maxToday", maxToday);
        
        Double avgToday = revenueRepository.getAvgToday();
        model.addAttribute("avgToday", avgToday);

        model.addAttribute("revenues", revenueRepository.findAll());

        Double total = revenueRepository.getTotalRevenue();
        model.addAttribute("totalRevenue", total);

        return "admin/admin-revenue";
    }
    
    
    @GetMapping("/admin/revenue/delete/{id}")
    public String deleteRevenue(@PathVariable("id") Integer id) {
        revenueRepository.deleteById(id);
        return "redirect:/admin/revenue";
    }
    
    
    @GetMapping("/admin/revenue/search")
    public String searchRevenue(@RequestParam("amount") Double amount, Model model) {

        model.addAttribute("revenues", revenueRepository.searchByAmount(amount));

        Double total = revenueRepository.getTotalRevenue();
        model.addAttribute("totalRevenue", total);

        return "admin/admin-revenue";
    }
    
    @GetMapping("/admin/revenue/filter")
    public String filterByDate(@RequestParam("date") String date, Model model) {

        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        List<Revenue> list = revenueRepository.findByDate(sqlDate);

        model.addAttribute("revenues", list);
        model.addAttribute("totalRevenue", revenueRepository.getTotalRevenue());
        model.addAttribute("maxToday", revenueRepository.getMaxToday());
        model.addAttribute("avgToday", revenueRepository.getAvgToday());

        return "admin/admin-revenue";
    }
   
}