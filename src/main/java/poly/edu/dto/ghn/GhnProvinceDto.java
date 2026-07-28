package poly.edu.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GhnProvinceDto {
    @JsonProperty("ProvinceID") private Integer provinceId;
    @JsonProperty("ProvinceName") private String provinceName;

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
}