package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Revenue;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.RevenueRepository;

import java.util.*;

@RestController
@RequestMapping("/api/admin/revenue")
public class RevenueAPIController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RevenueRepository revenueRepository;

    // ===== 1. LOAD + TÍNH DOANH THU =====
    @GetMapping
    public Map<String, Object> revenue() {

        // XÓA CŨ
        revenueRepository.deleteAll();

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

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

        // ===== TRẢ JSON =====
        Map<String, Object> data = new HashMap<>();

        data.put("revenues", revenueRepository.findAll());
        data.put("totalRevenue", revenueRepository.getTotalRevenue());
        data.put("maxToday", revenueRepository.getMaxToday());
        data.put("avgToday", revenueRepository.getAvgToday());
        data.put("chartLabels", revenueRepository.getChartDates());
        data.put("chartData", revenueRepository.getChartTotals());
        data.put("topProducts", revenueRepository.getTopProducts());

        return data;
    }

    // ===== 2. DELETE =====
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        revenueRepository.deleteById(id);
        return "Deleted successfully";
    }

    // ===== 3. SEARCH =====
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("amount") Double amount) {

        Map<String, Object> data = new HashMap<>();

        data.put("revenues", revenueRepository.searchByAmount(amount));
        data.put("totalRevenue", revenueRepository.getTotalRevenue());

        return data;
    }

    // ===== 4. FILTER THEO NGÀY =====
    @GetMapping("/filter")
    public Map<String, Object> filter(@RequestParam("date") String date) {

        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        Map<String, Object> data = new HashMap<>();

        data.put("revenues", revenueRepository.findByDate(sqlDate));
        data.put("totalRevenue", revenueRepository.getTotalRevenue());
        data.put("maxToday", revenueRepository.getMaxToday());
        data.put("avgToday", revenueRepository.getAvgToday());

        return data;
    }
}