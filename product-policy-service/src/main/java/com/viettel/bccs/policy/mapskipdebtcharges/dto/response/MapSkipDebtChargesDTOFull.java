package com.viettel.bccs.policy.mapskipdebtcharges.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapSkipDebtChargesDTOFull {

    @Schema(description = "Khóa nhóm dữ liệu", example = "1")
    @Size(max = 100, message = "key tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "key không chứa ký tự điều khiển")
    private String key;

    @Schema(description = "Danh sách bản ghi MAP_SKIP_DEBT_CHARGES ứng với key")
    @Size(max = 1000, message = "mapSkipDebtChargesDTO tối đa 1000 phần tử")
    private List<MapSkipDebtChargesDTO> mapSkipDebtChargesDTOList;

}
