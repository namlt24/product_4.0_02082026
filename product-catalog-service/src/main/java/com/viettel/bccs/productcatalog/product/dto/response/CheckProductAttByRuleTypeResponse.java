package com.viettel.bccs.productcatalog.product.dto.response;

import java.io.Serializable;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Kết quả kiểm tra gói cước theo rule type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckProductAttByRuleTypeResponse implements Serializable {

    private static final long TELECOM_SERVICE_MOBILE = 1L;
    private static final String PACKAGE_TYPE_DCOM = "DCOM";
    private static final String PACKAGE_TYPE_DI_DONG = "DI_DONG";
    private static final String PACKAGE_TYPE_NO_MATCH = "NO_MATCH";
    private static final String SPEC_CHAR_QOS = "QOS";
    private static final String SPEC_CHAR_GOI_CUOC_DAC_THU = "GOI_CUOC_DAC_THU";
    private static final String SPEC_CHAR_IS_VOLTE = "IS_VOLTE";

    @Schema(description = "Gói có đạt điều kiện của rule hay không", example = "true")
    private Boolean isValid;

    @Schema(description = "Loại gói: DI_DONG / DCOM / NO_MATCH", example = "DCOM")
    @Size(max = 50, message = "packageType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_]{0,50}$", message = "packageType chỉ gồm chữ, số, '_'")
    private String packageType;


    public static CheckProductAttByRuleTypeResponse evaluate(Long telecomServiceId, Set<String> specCharCodes) {
        if (!Long.valueOf(TELECOM_SERVICE_MOBILE).equals(telecomServiceId)) {
            return of(false, PACKAGE_TYPE_NO_MATCH);
        }
        if (specCharCodes.contains(SPEC_CHAR_QOS) || specCharCodes.contains(SPEC_CHAR_GOI_CUOC_DAC_THU)) {
            return of(specCharCodes.contains(SPEC_CHAR_IS_VOLTE), PACKAGE_TYPE_DCOM);
        }
        return of(true, PACKAGE_TYPE_DI_DONG);
    }

    private static CheckProductAttByRuleTypeResponse of(boolean isValid, String packageType) {
        return builder()
                .isValid(isValid)
                .packageType(packageType)
                .build();
    }
}
