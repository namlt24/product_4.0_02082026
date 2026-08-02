package com.viettel.bccs.productcatalog.optionset.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.viettel.bccs.productcatalog.optionset.dto.response.ResponseMDealerDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product-catalog-service/v1/optionsetvalue")
@RequiredArgsConstructor
public class OptionSetValueController {

    private final OptionSetValueService optionSetValueService;

    @GetMapping("/getByOptionSetId")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetId(@PathVariable Long optionSetId) {
        return StandardResponses.success(optionSetValueService.getByOptionSetId(optionSetId));
    }

    @GetMapping("/getByOptionSetIdAndStatus")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetIdAndStatus(
            @RequestParam Long optionSetId,
            @RequestParam String status) {
        return StandardResponses.success(optionSetValueService.getByOptionSetIdAndStatus(optionSetId, status));
    }

    @GetMapping("/findByOptionSetCode/{code}")
    public StandardResponse<List<OptionSetValueResponse>> findByOptionSetCode(@PathVariable String code) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCode(code));
    }

    @GetMapping("/findByOptionSetCodes")
    public StandardResponse<Map<String, List<OptionSetValueResponse>>> findByOptionSetCodes(
            @RequestParam List<String> codes) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCodes(codes));
    }

    @GetMapping("/getAllGroupCustType")
    @Operation(operationId = "API_PRODUCT_005",
            summary = "API lấy danh sách nhóm loại khách hàng/ loại khách hàng/ loại giấy tờ",
            description = "API lấy danh sách nhóm loại khách hàng/ loại khách hàng/ loại giấy tờ")
    public StandardResponse<List<OptionSetValueResponse>> getAllGroupCustType() {
        return StandardResponses.success(optionSetValueService.getAllGroupCustType());
    }

    @GetMapping("/getValueByTwoCodeOption")
    public StandardResponse<String> getValueByTwoCodeOption(
            @RequestParam String optSetCode,
            @RequestParam String name) {
        return StandardResponses.success(optionSetValueService.getValueByTwoCodeOption(optSetCode, name));
    }

    @PostMapping("/getSubObject")
    @Operation(operationId = "API_PRODUCT_014",
            summary = "API lấy danh sách đối tượng con",
            description = "API lấy danh sách đối tượng con (sub-object) phục vụ MDealer, dựa trên loại khách hàng và ngày sinh. " +
                    "Phân nhóm: cá nhân trong nước (groupType=1) / nước ngoài (groupType=3) theo độ tuổi, hoặc doanh nghiệp (groupType=2) theo option set.")
    public StandardResponse<ResponseMDealerDTO> getSubObject(
            @Parameter(description = "Loại khách hàng (custType)", example = "PREPAID", required = true)
            @RequestParam
            @Schema(description = "Loại khách hàng", maxLength = 10)
            String custType,

            @Parameter(description = "Ngày sinh khách hàng (ddMMyyyy)", example = "01011990")
            @RequestParam(required = false)
            @Schema(description = "Ngày sinh KH (ddMMyyyy)", maxLength = 8)
            @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])\\d{4}$",
                    message = "Date must be in the format ddMMyyyy")
            String birthDate) {
        return StandardResponses.success(optionSetValueService.getSubObject(custType, birthDate));
    }
}