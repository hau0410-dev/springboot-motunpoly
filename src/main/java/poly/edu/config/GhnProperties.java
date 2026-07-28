package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GhnProperties {

    @Value("${ghn.base-url}")
    private String baseUrl;

    @Value("${ghn.token}")
    private String token;

    @Value("${ghn.shop-id}")
    private Integer shopId;

    @Value("${ghn.tphcm-province-id}")
    private Integer tphcmProvinceId;

    @Value("${ghn.default-item-weight-gram}")
    private Integer defaultItemWeightGram;

    @Value("${ghn.default-box-length-cm}")
    private Integer defaultBoxLength;

    @Value("${ghn.default-box-width-cm}")
    private Integer defaultBoxWidth;

    @Value("${ghn.default-box-height-cm}")
    private Integer defaultBoxHeight;
    @Value("${ghn.shop.from-district-id}")
    private Integer shopFromDistrictId;

    @Value("${ghn.shop.from-ward-code}")
    private String shopFromWardCode;

    public Integer getShopFromDistrictId() { return shopFromDistrictId; }
    public String getShopFromWardCode() { return shopFromWardCode; }

    public String getBaseUrl() { return baseUrl; }
    public String getToken() { return token; }
    public Integer getShopId() { return shopId; }
    public Integer getTphcmProvinceId() { return tphcmProvinceId; }
    public Integer getDefaultItemWeightGram() { return defaultItemWeightGram; }
    public Integer getDefaultBoxLength() { return defaultBoxLength; }
    public Integer getDefaultBoxWidth() { return defaultBoxWidth; }
    public Integer getDefaultBoxHeight() { return defaultBoxHeight; }
}