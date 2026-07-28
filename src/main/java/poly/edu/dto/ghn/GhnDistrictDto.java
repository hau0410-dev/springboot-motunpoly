package poly.edu.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GhnDistrictDto {
    @JsonProperty("DistrictID") private Integer districtId;
    @JsonProperty("ProvinceID") private Integer provinceId;
    @JsonProperty("DistrictName") private String districtName;

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
}