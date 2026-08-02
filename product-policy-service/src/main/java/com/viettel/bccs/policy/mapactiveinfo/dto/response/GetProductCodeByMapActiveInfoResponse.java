package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import com.viettel.bccs.policy.client.dto.ProductOfferingDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private List<ProductOfferingDTO> productOfferingDTOs;
}