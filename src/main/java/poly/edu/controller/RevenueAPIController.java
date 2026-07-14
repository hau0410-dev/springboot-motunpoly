package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Revenue;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.RevenueRepository;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/revenue")
public class RevenueAPIController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RevenueRepository revenueRepository;

    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    // ===== 1. LẤY DỮ LIỆU DOANH THU (lọc theo preset/khoảng ngày/gom nhóm biểu đồ) =====
    @GetMapping
    public Map<String, Object> revenue(
            @RequestParam(value = "preset", required = false, defaultValue = "all") String preset,
            @RequestParam(value = "from", required = false) String fromStr,
            @RequestParam(value = "to", required = false) String toStr,
            @RequestParam(value = "groupBy", required = false, defaultValue = "day") String groupBy,
            @RequestParam(value = "minAmount", required = false) Double minAmount) {

        LocalDate[] range = resolveRange(preset, fromStr, toStr);
        Date sqlFrom = range[0] == null ? null : Date.valueOf(range[0]);
        Date sqlTo = range[1] == null ? null : Date.valueOf(range[1]);

        Map<String, Object> data = new HashMap<>();
        data.put("revenues", revenueRepository.findFiltered(sqlFrom, sqlTo, minAmount));
        putCommonData(data, sqlFrom, sqlTo, groupBy);

        return data;
    }

    // ===== 2. ĐỒNG BỘ DOANH THU TỪ ĐƠN HÀNG ĐÃ HOÀN THÀNH (cộng dồn, không tính trùng) =====
    @GetMapping("/sync")
    public Map<String, Object> sync() {

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

        Map<String, Object> data = new HashMap<>();
        data.put("revenues", revenueRepository.findFiltered(null, null, null));
        putCommonData(data, null, null, "day");

        return data;
    }

    // ===== 3. DELETE =====
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        revenueRepository.deleteById(id);
        return "Deleted successfully";
    }

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
                return new LocalDate[]{start, start.plusDays(6)};
            }
            case "month": {
                LocalDate start = today.withDayOfMonth(1);
                return new LocalDate[]{start, today.withDayOfMonth(today.lengthOfMonth())};
            }
            case "year": {
                LocalDate start = today.withDayOfYear(1);
                return new LocalDate[]{start, today.withDayOfYear(today.lengthOfYear())};
            }
            case "custom": {
                LocalDate from = (fromStr != null && !fromStr.isEmpty()) ? LocalDate.parse(fromStr) : null;
                LocalDate to = (toStr != null && !toStr.isEmpty()) ? LocalDate.parse(toStr) : null;
                return new LocalDate[]{from, to};
            }
            default:
                return new LocalDate[]{null, null};
        }
    }

    // ===== Dữ liệu dùng chung: tổng, cao nhất/TB trong khoảng, top sản phẩm, biểu đồ theo groupBy =====
    private void putCommonData(Map<String, Object> data, Date sqlFrom, Date sqlTo, String groupBy) {

        data.put("totalRevenue", revenueRepository.getTotalInRange(sqlFrom, sqlTo));
        data.put("maxInRange", revenueRepository.getMaxInRange(sqlFrom, sqlTo));
        data.put("avgInRange", revenueRepository.getAvgInRange(sqlFrom, sqlTo));
        data.put("topProducts", revenueRepository.getTopProducts(sqlFrom, sqlTo));

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        if (groupBy == null) {
            groupBy = "day";
        }

        switch (groupBy) {
            case "week":
                for (Object[] row : revenueRepository.getChartByWeek(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    int wk = ((Number) row[1]).intValue();
                    double total = row[2] == null ? 0d : ((Number) row[2]).doubleValue();
                    chartLabels.add("Tuần " + wk + "/" + yr);
                    chartData.add(total);
                }
                break;
            case "month":
                for (Object[] row : revenueRepository.getChartByMonth(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    int mo = ((Number) row[1]).intValue();
                    double total = row[2] == null ? 0d : ((Number) row[2]).doubleValue();
                    chartLabels.add(String.format("%02d/%d", mo, yr));
                    chartData.add(total);
                }
                break;
            case "year":
                for (Object[] row : revenueRepository.getChartByYear(sqlFrom, sqlTo)) {
                    int yr = ((Number) row[0]).intValue();
                    double total = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
                    chartLabels.add(String.valueOf(yr));
                    chartData.add(total);
                }
                break;
            default:
                for (Object[] row : revenueRepository.getChartByDay(sqlFrom, sqlTo)) {
                    LocalDate d = toLocalDate(row[0]);
                    double total = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
                    chartLabels.add(d.format(DAY_LABEL_FORMAT));
                    chartData.add(total);
                }
                break;
        }

        data.put("chartLabels", chartLabels);
        data.put("chartData", chartData);
    }

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