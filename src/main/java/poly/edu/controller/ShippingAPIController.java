package poly.edu.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import poly.edu.config.GhnProperties;
import poly.edu.dto.ghn.GhnFeeRequest;
import poly.edu.dto.ghn.GhnProvinceDto;
import poly.edu.dto.ghn.GhnWardOption;
import poly.edu.service.GhnService;

@RestController
@RequestMapping("/api/shipping")
public class ShippingAPIController {

    @Autowired private GhnService ghnService;
    @Autowired private GhnProperties ghnProperties;

    // ===== 1. DANH SÁCH TỈNH/THÀNH (thật từ GHN) =====
    @GetMapping("/provinces")
    public List<GhnProvinceDto> provinces() {
        return ghnService.getProvinces();
    }

    // ===== 2. DANH SÁCH PHƯỜNG/XÃ THEO TỈNH (đã gộp phẳng, bỏ qua bước chọn quận) =====
    @GetMapping("/wards")
    public List<GhnWardOption> wards(@RequestParam("provinceId") Integer provinceId) {
        return ghnService.getWardOptionsByProvince(provinceId);
    }

    // ===== 3. TÍNH PHÍ SHIP (chỉ dùng khi KHÔNG phải TP.HCM) =====
    @PostMapping("/fee")
    public ResponseEntity<?> fee(@RequestBody GhnFeeRequest req) {
        try {
            int fee = ghnService.calculateFee(req.getToDistrictId(), req.getToWardCode(), req.getWeightGram());
            boolean isTphcm = ghnService.isTPHCM(req.getProvinceId());
            return ResponseEntity.ok(Map.of("fee", fee, "isTphcm", isTphcm));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ===== 4. TRẢ VỀ MÃ TỈNH TP.HCM để FE so sánh (không hardcode 202 ở JS) =====
    @GetMapping("/tphcm-province-id")
    public Map<String, Integer> tphcmId() {
        return Map.of("provinceId", ghnProperties.getTphcmProvinceId());
    }
}