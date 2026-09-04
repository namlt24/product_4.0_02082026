package com.viettel.bccs.productcatalog.optionset.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.GetSubObjectResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiFindByOptionSetCode;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiFindByOptionSetCodes;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetAllGroupCustType;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetByOptionSetId;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetByOptionSetIdAndStatus;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetSubObject;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetValueByTwoCodeOption;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/optionsetvalue")
@RequiredArgsConstructor
@Tag(name = "OptionSetValue", description = "Tra cứu giá trị của nhóm option set (danh mục dùng chung)")
public class OptionSetValueController {

    private final OptionSetValueService optionSetValueService;

    @ApiGetByOptionSetId
    @GetMapping("/getByOptionSetId")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetId(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long optionSetId) {
        return StandardResponses.success(optionSetValueService.getByOptionSetId(optionSetId));
    }

    @ApiGetByOptionSetIdAndStatus
    @GetMapping("/getByOptionSetIdAndStatus")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetIdAndStatus(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long optionSetId,
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(optionSetValueService.getByOptionSetIdAndStatus(optionSetId, status));
    }

    @ApiFindByOptionSetCode
    @GetMapping("/findByOptionSetCode/{code}")
    public StandardResponse<List<OptionSetValueResponse>> findByOptionSetCode(
            @Parameter(description = "Mã nhóm option set", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @PathVariable
            String code) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCode(code));
    }

    @ApiFindByOptionSetCodes
    @GetMapping("/findByOptionSetCodes")
    public StandardResponse<Map<String, List<OptionSetValueResponse>>> findByOptionSetCodes(
            @Parameter(description = "Danh sách mã nhóm option set", example = "[\"CUST_TYPE_GROUP_TYPE\"]",
                required = true)            @RequestParam(required = false)
            List<String> codes) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCodes(codes));
    }

    @ApiGetAllGroupCustType
    @GetMapping("/getAllGroupCustType")
    public StandardResponse<List<OptionSetValueResponse>> getAllGroupCustType() {
        return StandardResponses.success(optionSetValueService.getAllGroupCustType());
    }

    @ApiGetValueByTwoCodeOption
    @GetMapping("/getValueByTwoCodeOption")
    public StandardResponse<String> getValueByTwoCodeOption(
            @Parameter(description = "Mã nhóm option set", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @RequestParam(required = false)
            String optSetCode,
            @Parameter(description = "Tên giá trị option set", example = "Cá nhân", required = true)
            @RequestParam(required = false)
            String name) {
        return StandardResponses.success(optionSetValueService.getValueByTwoCodeOption(optSetCode, name));
    }

    @ApiGetSubObject
    @PostMapping("/getSubObject")
    public StandardResponse<GetSubObjectResponse> getSubObject(
            @Parameter(description = "Loại khách hàng (custType)", example = "PREPAID", required = true)
            @RequestParam(required = false)
            @Schema(description = "Loại khách hàng", maxLength = 10)
            String custType,

            @Parameter(description = "Ngày sinh khách hàng (ddMMyyyy)", example = "01011990")
            @RequestParam(required = false)
            @Schema(description = "Ngày sinh KH (ddMMyyyy)", maxLength = 8)
            String birthDate) {
        return StandardResponses.success(optionSetValueService.getSubObject(custType, birthDate));
    }
}
