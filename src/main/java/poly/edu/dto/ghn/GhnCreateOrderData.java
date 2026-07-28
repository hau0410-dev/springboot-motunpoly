package poly.edu.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GhnCreateOrderData {
    @JsonProperty("order_code") private String orderCode;
    @JsonProperty("total_fee") private Integer totalFee;
    @JsonProperty("expected_delivery_time") private String expectedDeliveryTime;

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Integer getTotalFee() { return totalFee; }
    public void setTotalFee(Integer totalFee) { this.totalFee = totalFee; }
    public String getExpectedDeliveryTime() { return expectedDeliveryTime; }
    public void setExpectedDeliveryTime(String expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }
}