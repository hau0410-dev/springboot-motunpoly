package poly.edu.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import poly.edu.service.ShippingService;

@Service
public class ShippingServiceImpl implements ShippingService {

    // ===== BẢNG GIÁ SHIP THEO QUẬN/HUYỆN (TP.HCM) =====
    // LinkedHashMap để giữ đúng thứ tự hiển thị trên dropdown chọn khu vực giao hàng.
    private static final Map<String, Long> FEE_TABLE = new LinkedHashMap<>();

    static {
        // Đồng giá 30.000đ
        FEE_TABLE.put("Quận 1", 30000L);
        FEE_TABLE.put("Quận 3", 30000L);
        FEE_TABLE.put("Quận 4", 30000L);
        FEE_TABLE.put("Quận 5", 30000L);
        FEE_TABLE.put("Quận 6", 30000L);
        FEE_TABLE.put("Quận 7", 30000L);
        FEE_TABLE.put("Quận 8", 30000L);
        FEE_TABLE.put("Tân Bình", 30000L);
        FEE_TABLE.put("Tân Phú", 30000L);
        FEE_TABLE.put("Gò Vấp", 30000L);

        // 40.000đ
        FEE_TABLE.put("Quận 12", 40000L);
        FEE_TABLE.put("Thủ Đức", 40000L);
        FEE_TABLE.put("Quận 2", 40000L);

        // 45.000đ
        FEE_TABLE.put("Bình Chánh", 45000L);
        FEE_TABLE.put("Hóc Môn", 45000L);
        FEE_TABLE.put("Quận 9", 45000L);

        // 50.000đ
        FEE_TABLE.put("Củ Chi", 50000L);

        // Khu vực chưa được liệt kê cụ thể (Bình Thạnh, Quận 10, Quận 11, Bình Tân, Nhà Bè,
        // Cần Giờ, ngoài TP.HCM...) -> tạm áp phí mặc định cao nhất trong bảng để an toàn.
        // TODO: bạn xác nhận lại mức phí này hoặc bổ sung riêng cho từng khu vực nếu cần.
        FEE_TABLE.put("Khu vực khác", 50000L);
    }

    @Override
    public List<String> getSupportedDistricts() {
        return new ArrayList<>(FEE_TABLE.keySet());
    }

    @Override
    public Map<String, Long> getFeeTable() {
        return new LinkedHashMap<>(FEE_TABLE);
    }

    @Override
    public long calculateFeeByDistrict(String district) {
        if (district == null) {
            return FEE_TABLE.get("Khu vực khác");
        }
        Long fee = FEE_TABLE.get(district.trim());
        return (fee != null) ? fee : FEE_TABLE.get("Khu vực khác");
    }

    @Override
    public long calculateFee(String district, boolean freeShip) {
        if (freeShip) {
            return 0L;
        }
        return calculateFeeByDistrict(district);
    }
}