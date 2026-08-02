package com.viettel.bccs.organization.staff.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.viettel.bccs.organization.staff.dto.StockDTO;

import java.util.ArrayList;
import java.util.List;

@Schema
public class StockCodeResponse {

    @Schema(description = "Danh sách kho", example = "[]")
    private List<StockDTO> lstShop = new ArrayList<>();

    public StockCodeResponse() {
    }

    public StockCodeResponse(List<StockDTO> lstShop) {
        this.lstShop = lstShop;
    }

    public List<StockDTO> getLstShop() { return lstShop; }
    public void setLstShop(List<StockDTO> lstShop) { this.lstShop = lstShop; }
}