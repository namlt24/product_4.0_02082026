package com.viettel.bccs.productcatalog.optionset.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Response trả về danh sách đối tượng con cho MDealer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMDealerDTO {

    @Schema(description = "Danh sách giá trị option set (đối tượng con)", example = "[]")
    @JsonProperty("lstOptionSetValue")
    private List<OptionSetValueResponse> lstOptionSetValue;

    @Schema(description = "Có cần tên người giám hộ hay không", example = "false")
    @JsonProperty("needGuardianName")
    private Boolean needGuardianName;

    @Schema(description = "Mã phản hồi", example = "000")
    @JsonProperty("code")
    private String code;
}