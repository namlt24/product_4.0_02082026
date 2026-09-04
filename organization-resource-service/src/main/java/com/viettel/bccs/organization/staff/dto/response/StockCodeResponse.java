package com.viettel.bccs.organization.staff.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.viettel.bccs.organization.staff.dto.StockDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema
public class StockCodeResponse {

    @Schema(description = "Danh sách kho", example = "[]")
    @Size(max = 1000, message = "lstShop tối đa 1000 phần tử")
    private List<StockDTO> lstShop = new ArrayList<>();

    public StockCodeResponse() {
    }

    public StockCodeResponse(List<StockDTO> lstShop) {
        this.lstShop = lstShop;
    }

    public List<StockDTO> getLstShop() {
        return lstShop;
    }

    public void setLstShop(List<StockDTO> lstShop) {
        this.lstShop = lstShop;
    }
}
