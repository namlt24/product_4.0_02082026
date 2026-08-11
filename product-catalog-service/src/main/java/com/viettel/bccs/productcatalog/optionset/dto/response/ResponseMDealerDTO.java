package com.viettel.bccs.productcatalog.optionset.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 1000, message = "lstOptionSetValue tối đa 1000 phần tử")
    private List<OptionSetValueResponse> lstOptionSetValue;

    @Schema(description = "Có cần tên người giám hộ hay không", example = "false")
    @JsonProperty("needGuardianName")
    private Boolean needGuardianName;

    @Schema(description = "Mã phản hồi", example = "000")
    @JsonProperty("code")
    @Size(max = 20, message = "code tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;
}