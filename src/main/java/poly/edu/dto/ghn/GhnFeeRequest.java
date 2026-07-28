package poly.edu.dto.ghn;

public class GhnFeeRequest {
    private Integer provinceId;
    private Integer toDistrictId;
    private String toWardCode;
    private Integer weightGram; // tổng khối lượng ước tính (số lượng SP * cân nặng mặc định)

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public Integer getToDistrictId() { return toDistrictId; }
    public void setToDistrictId(Integer toDistrictId) { this.toDistrictId = toDistrictId; }
    public String getToWardCode() { return toWardCode; }
    public void setToWardCode(String toWardCode) { this.toWardCode = toWardCode; }
    public Integer getWeightGram() { return weightGram; }
    public void setWeightGram(Integer weightGram) { this.weightGram = weightGram; }
}