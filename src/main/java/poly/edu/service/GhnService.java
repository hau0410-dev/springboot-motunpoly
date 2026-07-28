package poly.edu.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import poly.edu.config.GhnProperties;
import poly.edu.dto.ghn.*;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Product;
import poly.edu.entity.ReturnOrder;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ReturnOrderRepository;

@Service
public class GhnService {

    @Autowired private RestTemplate restTemplate;
    @Autowired private GhnProperties ghn;
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRespository orderItemRepo;
    @Autowired private ReturnOrderRepository returnOrderRepo;
    @Autowired private ProductService productService;

    private HttpHeaders headersWithShop() {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Token", ghn.getToken());
        h.set("ShopId", String.valueOf(ghn.getShopId()));
        return h;
    }

    private HttpHeaders headersTokenOnly() {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Token", ghn.getToken());
        return h;
    }

    // ===== TỈNH/THÀNH =====
    public List<GhnProvinceDto> getProvinces() {
        String url = ghn.getBaseUrl() + "/shiip/public-api/master-data/province";
        HttpEntity<Void> entity = new HttpEntity<>(headersTokenOnly());
        GhnApiResponse<List<GhnProvinceDto>> resp = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<GhnApiResponse<List<GhnProvinceDto>>>() {}).getBody();
        return resp != null && resp.getData() != null ? resp.getData() : Collections.emptyList();
    }

    private List<GhnDistrictDto> getDistricts(Integer provinceId) {
        String url = ghn.getBaseUrl() + "/shiip/public-api/master-data/district";
        Map<String, Object> body = Map.of("province_id", provinceId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersTokenOnly());
        // FIX: đổi GET -> POST vì SimpleClientHttpRequestFactory (Java) không hỗ trợ gửi body kèm GET
        GhnApiResponse<List<GhnDistrictDto>> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<GhnApiResponse<List<GhnDistrictDto>>>() {}).getBody();
        return resp != null && resp.getData() != null ? resp.getData() : Collections.emptyList();
    }

    private List<GhnWardDto> getWards(Integer districtId) {
        String url = ghn.getBaseUrl() + "/shiip/public-api/master-data/ward";
        Map<String, Object> body = Map.of("district_id", districtId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersTokenOnly());
        // FIX: đổi GET -> POST (cùng lý do như trên)
        GhnApiResponse<List<GhnWardDto>> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<GhnApiResponse<List<GhnWardDto>>>() {}).getBody();
        return resp != null && resp.getData() != null ? resp.getData() : Collections.emptyList();
    }

    // ===== FE CHỈ THẤY "TỈNH -> PHƯỜNG": tự động lấy quận rồi lấy phường của TỪNG quận,
    // gộp phẳng lại thành 1 danh sách phường duy nhất cho cả tỉnh =====
    public List<GhnWardOption> getWardOptionsByProvince(Integer provinceId) {
        List<GhnWardOption> result = new ArrayList<>();
        List<GhnDistrictDto> districts = getDistricts(provinceId);

        for (GhnDistrictDto d : districts) {
            List<GhnWardDto> wards = getWards(d.getDistrictId());
            for (GhnWardDto w : wards) {
                result.add(new GhnWardOption(w.getWardCode(), w.getWardName(), d.getDistrictId(), d.getDistrictName()));
            }
        }
        return result;
    }

    // ===== TÍNH PHÍ SHIP (chỉ cần to_district_id + to_ward_code, "from" GHN tự lấy theo ShopId) =====
    public int calculateFee(Integer toDistrictId, String toWardCode, Integer weightGram) {
        String url = ghn.getBaseUrl() + "/shiip/public-api/v2/shipping-order/fee";

        Map<String, Object> body = new HashMap<>();
        // FIX: truyền thẳng địa chỉ "from" (kho hàng của bạn) thay vì để GHN tự lấy theo ShopId,
        // vì hồ sơ Shop hiện chưa có địa chỉ -> gây lỗi SHOP_INFO_ERROR.
        body.put("from_district_id", ghn.getShopFromDistrictId());
        body.put("from_ward_code", ghn.getShopFromWardCode());
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("weight", weightGram != null ? weightGram : ghn.getDefaultItemWeightGram());
        body.put("length", ghn.getDefaultBoxLength());
        body.put("width", ghn.getDefaultBoxWidth());
        body.put("height", ghn.getDefaultBoxHeight());
        body.put("service_type_id", 2);
        body.put("insurance_value", 0);
        body.put("cod_value", 0);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersWithShop());
        GhnApiResponse<GhnFeeData> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<GhnApiResponse<GhnFeeData>>() {}).getBody();

        if (resp == null || !resp.isSuccess() || resp.getData() == null) {
            throw new RuntimeException("GHN tính phí thất bại: " + (resp != null ? resp.getMessage() : "no response"));
        }
        return resp.getData().getTotal();
    }

    public boolean isTPHCM(Integer provinceId) {
        return provinceId != null && provinceId.equals(ghn.getTphcmProvinceId());
    }

    // ===== TẠO ĐƠN GHN CHO 1 ĐƠN HÀNG ĐÃ CÓ SẴN (admin bấm "Gửi GHN") =====
    public GhnCreateOrderData createGhnOrder(Integer orderId, Integer codAmount, Integer weightGram, String note) {

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) throw new RuntimeException("Không tìm thấy đơn hàng id=" + orderId);
        if (order.getGhnOrderCode() != null) throw new RuntimeException("Đơn đã gửi GHN trước đó: " + order.getGhnOrderCode());
        if (order.getGhnToDistrictId() == null || order.getGhnToWardCode() == null) {
            throw new RuntimeException("Đơn hàng chưa có địa chỉ GHN (to_district_id/to_ward_code) - khách đặt hàng trong TP.HCM (giao nội bộ) hoặc thiếu dữ liệu.");
        }

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        String url = ghn.getBaseUrl() + "/shiip/public-api/v2/shipping-order/create";

        Map<String, Object> body = new HashMap<>();
        body.put("payment_type_id", 1);
        body.put("note", note != null ? note : "Đơn hàng MotunPoly #" + orderId);
        body.put("required_note", "KHONGCHOXEMHANG");
        body.put("client_order_code", String.valueOf(orderId));
        body.put("to_name", order.getFullname());
        body.put("to_phone", order.getPhone());
        body.put("to_address", order.getAddress());
        body.put("to_ward_code", order.getGhnToWardCode());
        body.put("to_district_id", order.getGhnToDistrictId());
        body.put("from_district_id", ghn.getShopFromDistrictId());
        body.put("from_ward_code", ghn.getShopFromWardCode());
        body.put("cod_amount", codAmount != null ? codAmount : 0);
        body.put("content", "Phụ tùng xe máy MotunPoly");
        body.put("weight", weightGram != null ? weightGram : ghn.getDefaultItemWeightGram() * Math.max(items.size(), 1));
        body.put("length", ghn.getDefaultBoxLength());
        body.put("width", ghn.getDefaultBoxWidth());
        body.put("height", ghn.getDefaultBoxHeight());
        body.put("service_type_id", 2);

        List<Map<String, Object>> ghnItems = new ArrayList<>();
        for (OrderItem it : items) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", it.getProduct() != null ? it.getProduct().getName() : "San pham");
            item.put("quantity", it.getQuantity());
            item.put("price", it.getPrice() != null ? it.getPrice().intValue() : 0);
            ghnItems.add(item);
        }
        body.put("items", ghnItems);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersWithShop());
        GhnApiResponse<GhnCreateOrderData> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<GhnApiResponse<GhnCreateOrderData>>() {}).getBody();

        if (resp == null || !resp.isSuccess() || resp.getData() == null) {
            throw new RuntimeException("Tạo đơn GHN thất bại: " + (resp != null ? resp.getMessage() : "no response"));
        }

        order.setGhnOrderCode(resp.getData().getOrderCode());
        order.setGhnStatus("ready_to_pick");
        order.setStatus("CHO_LAY_HANG"); // FIX: đồng bộ đúng luồng status hiện tại (giống đơn nội bộ)
        if (resp.getData().getTotalFee() != null) {
            order.setGhnFee(resp.getData().getTotalFee().doubleValue());
        }
        orderRepo.save(order);

        return resp.getData();
    }

    public Map<String, Object> trackGhnOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null || order.getGhnOrderCode() == null) {
            throw new RuntimeException("Đơn hàng chưa được gửi qua GHN");
        }
        String url = ghn.getBaseUrl() + "/shiip/public-api/v2/shipping-order/detail";
        Map<String, Object> body = Map.of("order_code", order.getGhnOrderCode());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headersTokenOnly());
        return restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody();
    }

    // =====================================================================================
    // ===== MÔ PHỎNG TRẠNG THÁI GHN QUA POSTMAN (vì webhook thật GHN không gọi được vào
    // localhost) — chỉ 3 sự kiện: PICKED_UP, DELIVERED, RETURN_ARRIVED, mirror ĐÚNG luồng
    // trạng thái của shipper nội bộ hiện có (CHO_LAY_HANG -> DANG_GIAO -> DA_GIAO, và
    // ReturnOrder: ... -> DA_LAY_HANG -> HOAN_KHO) =====
    // =====================================================================================
    public void handleStatusEvent(Integer orderId, String event) {

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) throw new RuntimeException("Không tìm thấy đơn hàng id=" + orderId);
        if (!"GHN".equals(order.getShippingMethod())) {
            throw new RuntimeException("Đơn hàng này không giao qua GHN");
        }

        switch (event) {

            case "PICKED_UP": {
                // Giống bước shipper.pickup(): CHO_LAY_HANG -> DANG_GIAO
                order.setStatus("DANG_GIAO");
                order.setGhnStatus("picked");
                orderRepo.save(order);
                break;
            }

            case "DELIVERED": {
                // Giống bước shipper.delivered(): DANG_GIAO -> DA_GIAO (+ COD thành công)
                order.setStatus("DA_GIAO");
                order.setGhnStatus("delivered");
                orderRepo.save(order);
                break;
            }

            case "RETURN_ARRIVED": {
                // Hàng đã được GHN vận chuyển từ tỉnh trả VỀ LẠI kho TP.HCM.
                // Vì GHN tự lấy + tự vận chuyển hàng hoàn (không qua shipper nội bộ),
                // nên tại đây coi như đã "DA_LAY_HANG" và "về kho" luôn -> tự động restock,
                // mirror đúng logic ShipperController.restockedReturn().
                ReturnOrder ro = returnOrderRepo.findByOrder_Id(orderId);

                if (ro == null) {
                    ro = new ReturnOrder();
                    ro.setOrder(order);
                    ro.setUser(order.getUser());
                    ro.setReason("GHN hoàn hàng (giao thất bại/khách từ chối nhận)");
                    ro.setRequestedDate(java.time.LocalDateTime.now());
                    ro.setConfirmedDate(java.time.LocalDateTime.now());
                }

                ro.setStatus("HOAN_KHO");
                ro.setPickedDate(java.time.LocalDateTime.now());
                ro.setRestockedDate(java.time.LocalDateTime.now());
                returnOrderRepo.save(ro);

                // Cộng lại tồn kho — giống hệt ShipperController.restockedReturn()
                List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
                for (OrderItem item : items) {
                    Product p = item.getProduct();
                    if (p != null) {
                        int stock = (p.getStock() == null) ? 0 : p.getStock();
                        p.setStock(stock + item.getQuantity());
                        productService.save(p);
                    }
                }

                order.setStatus("DA_HOAN_HANG");
                order.setGhnStatus("returned");
                orderRepo.save(order);
                break;
            }

            default:
                throw new RuntimeException("Sự kiện không hợp lệ. Chỉ nhận: PICKED_UP, DELIVERED, RETURN_ARRIVED");
        }
    }
}