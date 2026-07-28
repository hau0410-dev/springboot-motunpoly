package poly.edu.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GhnWardDto {
    @JsonProperty("WardCode") private String wardCode;
    @JsonProperty("DistrictID") private Integer districtId;
    @JsonProperty("WardName") private String wardName;

    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
}