package poly.edu.service;

import java.util.List;
import java.util.Map;

public interface ShippingService {

    // Danh sách quận/huyện hợp lệ để đổ vào dropdown chọn khu vực giao hàng
    List<String> getSupportedDistricts();

    // Toàn bộ bảng giá (quận -> phí) - dùng để hiển thị realtime phía client (JS),
    // giá trị THẬT vẫn luôn được tính lại ở server khi đặt hàng, không tin dữ liệu client gửi lên.
    Map<String, Long> getFeeTable();

    // Tính phí ship (đồng) theo quận/huyện, CHƯA xét khách hàng thân thiết
    long calculateFeeByDistrict(String district);

    // Tính phí ship cuối cùng: nếu freeShip = true (khách hàng thân thiết) -> 0đ,
    // ngược lại tính theo quận/huyện
    long calculateFee(String district, boolean freeShip);
}