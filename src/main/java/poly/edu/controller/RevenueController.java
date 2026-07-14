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

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class RevenueController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    RevenueRepository revenueRepository;

    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    // ===== TRANG DOANH THU (dashboard lọc theo khoảng thời gian + gom nhóm biểu đồ) =====
    // preset: today | week | month | year | all | custom (mặc định "all")
    // groupBy: day | week | month | year (mặc định "day")
    @GetMapping("/admin/revenue")
    public String revenue(
            @RequestParam(value = "preset", required = false, defaultValue = "all") String preset,
            @RequestParam(value = "from", required = false) String fromStr,
            @RequestParam(value = "to", required = false) String toStr,
            @RequestParam(value = "groupBy", required = false, defaultValue = "day") String groupBy,
            @RequestParam(value = "minAmount", required = false) Double minAmount,
            Model model) {

        LocalDate[] range = resolveRange(preset, fromStr, toStr);
        LocalDate from = range[0];
        LocalDate to = range[1];

        Date sqlFrom = from == null ? null : Date.valueOf(from);
        Date sqlTo = to == null ? null : Date.valueOf(to);

        loadChartAndStats(model, sqlFrom, sqlTo, groupBy);

        model.addAttribute("revenues", revenueRepository.findFiltered(sqlFrom, sqlTo, minAmount));

        // Trả lại giá trị đang chọn để giao diện tô sáng đúng nút / giữ nguyên form sau khi submit
        model.addAttribute("preset", preset);
        model.addAttribute("groupBy", groupBy);
        model.addAttribute("fromParam", from == null ? "" : from.toString());
        model.addAttribute("toParam", to == null ? "" : to.toString());
        model.addAttribute("minAmount", minAmount);

        return "admin/admin-revenue";
    }

    // ===== ĐỒNG BỘ DOANH THU TỪ ĐƠN HÀNG ĐÃ HOÀN THÀNH (cộng dồn, không tính trùng) =====
    @GetMapping("/admin/revenue/sync")
    public String syncRevenue() {

        syncFromCompletedOrders();

        return "redirect:/admin/revenue";
    }

    @GetMapping("/admin/revenue/delete/{id}")
    public String deleteRevenue(@PathVariable("id") Integer id) {
        revenueRepository.deleteById(id);
        return "redirect:/admin/revenue";
    }

    // ===== Quy đổi preset -> khoảng ngày [from, to] (LocalDate, có thể null = không giới hạn) =====
    private LocalDate[] resolveRange(String preset, String fromStr, String toStr) {

        LocalDate today = LocalDate.now();

        if (preset == null) {
            preset = "all";
        }

        switch (preset) {
            case "today":
                return new LocalDate[]{today, today};

            case "week": {
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = start.plusDays(6);
                return new LocalDate[]{start, end};
            }

            case "month": {
                LocalDate start = today.withDayOfMonth(1);
                LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
                return new LocalDate[]{start, end};
            }

            case "year": {
                LocalDate start = today.withDayOfYear(1);
                LocalDate end = today.withDayOfYear(today.lengthOfYear());
                return new LocalDate[]{start, end};
            }

            case "custom": {
                LocalDate from = (fromStr != null && !fromStr.isEmpty()) ? LocalDate.parse(fromStr) : null;
                LocalDate to = (toStr != null && !toStr.isEmpty()) ? LocalDate.parse(toStr) : null;
                return new LocalDate[]{from, to};
            }

            default: // "all"
                return new LocalDate[]{null, null};
        }
    }

    // ===== Đồng bộ dữ liệu doanh thu: nguồn = OrderItem của các đơn đã HOAN_THANH,
    //       chỉ thêm mới, không tính trùng (dựa vào orderItemId đã lưu) =====
    private void syncFromCompletedOrders() {

        Set<Integer> syncedIds = new HashSet<>(revenueRepository.findAllSyncedOrderItemIds());

        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {

            if (!"HOAN_THANH".equals(order.getStatus())) {
                continue;
            }

            for (OrderItem item : order.getOrderItems()) {

                if (syncedIds.contains(item.getId())) {
                    continue;
                }

                Revenue r = new Revenue();
                r.setOrderItemId(item.getId());
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

    // ===== Biểu đồ + top sản phẩm + số liệu thống kê, theo đúng khoảng thời gian + cách gom nhóm đang chọn =====
    private void loadChartAndStats(Model model, Date sqlFrom, Date sqlTo, String groupBy) {

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        if (groupBy == null) {
            groupBy = "day";
        }

        switch (groupBy) {

            case "week": {
                for (Object[] row : revenueRepository.getChartByWeek(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    int wk = ((Number) row[1]).intValue();
                    double total = row[2] == null ? 0d : ((Number) row[2]).doubleValue();
                    chartLabels.add("Tuần " + wk + "/" + yr);
                    chartData.add(total);
                }
                break;
            }

            case "month": {
                for (Object[] row : revenueRepository.getChartByMonth(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    int mo = ((Number) row[1]).intValue();
                    double total = row[2] == null ? 0d : ((Number) row[2]).doubleValue();
                    chartLabels.add(String.format("%02d/%d", mo, yr));
                    chartData.add(total);
                }
                break;
            }

            case "year": {
                for (Object[] row : revenueRepository.getChartByYear(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    double total = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
                    chartLabels.add(String.valueOf(yr));
                    chartData.add(total);
                }
                break;
            }

            default: { // "day"
                for (Object[] row : revenueRepository.getChartByDay(sqlFrom, sqlTo)) {
                    LocalDate d = toLocalDate(row[0]);
                    double total = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
                    chartLabels.add(d.format(DAY_LABEL_FORMAT));
                    chartData.add(total);
                }
                break;
            }
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        model.addAttribute("topProducts", revenueRepository.getTopProducts(sqlFrom, sqlTo));

        model.addAttribute("totalRevenue", revenueRepository.getTotalInRange(sqlFrom, sqlTo));
        model.addAttribute("maxInRange", revenueRepository.getMaxInRange(sqlFrom, sqlTo));
        model.addAttribute("avgInRange", revenueRepository.getAvgInRange(sqlFrom, sqlTo));
    }

    // Hibernate/driver tuỳ phiên bản có thể trả cột SQL DATE về java.sql.Date
    // hoặc java.time.LocalDate -> xử lý an toàn cho cả 2 trường hợp.
    private LocalDate toLocalDate(Object raw) {
        if (raw instanceof LocalDate) {
            return (LocalDate) raw;
        }
        if (raw instanceof Date) {
            return ((Date) raw).toLocalDate();
        }
        if (raw instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) raw).toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(raw.toString());
    }
}