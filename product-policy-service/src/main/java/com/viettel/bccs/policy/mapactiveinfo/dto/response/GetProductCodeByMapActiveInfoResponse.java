package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response cho API getProductCodeByMapActiveInfo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class GetProductCodeByMapActiveInfoResponse {

    @Schema(description = "Danh sach goi cuoc")
    @Size(max = 1000, message = "productOfferingDTOs tối đa 1000 phần tử")
    private List<ProductCodeDTO> productOfferingDTOs;
}