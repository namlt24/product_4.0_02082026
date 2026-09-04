package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response cho API getProductCodeByMapActiveInfo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class GetProductCodeByMapActiveInfoResponse {

    // Ten field giu checkstyle-compliant (productOfferingDtos, toi da 2 chu hoa lien tiep), nhung
    // @JsonProperty pin lai dung ten JSON goc "productOfferingDTOs" de khong doi hop dong wire.
    @Schema(description = "Danh sach goi cuoc")
    @Size(max = 1000, message = "productOfferingDTOs tối đa 1000 phần tử")
    @JsonProperty("productOfferingDTOs")
    private List<ProductCodeDTO> productOfferingDtos;
}