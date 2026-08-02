package com.viettel.bccs.organization.staff.dto;

import com.viettel.bccs.organization.shop.dto.ShopDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema
@Data
public class StockDTO {
    private Long stockId;
    private String code;
    private String name;
    private String type;

    public StockDTO() {
    }

    public StockDTO(Long stockId, String code, String name, String type) {
        this.stockId = stockId;
        this.code = code;
        this.name = name;
        this.type = type;
    }
}