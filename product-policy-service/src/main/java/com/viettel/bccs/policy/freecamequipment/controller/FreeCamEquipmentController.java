package com.viettel.bccs.policy.freecamequipment.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.freecamequipment.dto.response.FreeCamEquipmentDTO;
import com.viettel.bccs.policy.freecamequipment.service.FreeCamEquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Free Cam Equipment", description = "APIs quản lý giá thiết bị CAM miễn phí theo lý do đấu nối")
@RestController
@RequestMapping("/product-policy-service/v1/free-cam-equipment")
@RequiredArgsConstructor
@Validated
public class FreeCamEquipmentController {

    private final FreeCamEquipmentService service;

    private static final String CHECK_REASON_FREE_CAM_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "freeCamEquipmentId": 1,
                  "actionCode": "5001",
                  "reasonId": 9003997310,
                  "areaCode": "H004",
                  "status": "1",
                  "camInsideNumber": 2,
                  "camOutsideNumber": 3,
                  "camMaxNumber": 5,
                  "camInsidePrice": 88888,
                  "camOutsidePrice": 99999,
                  "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "createUser": "system",
                  "updateUser": null,
                  "description": null,
                  "shopCode": null,
                  "staffCode": null,
                  "createDatetime": "2026-08-01T00:00:00.000+00:00",
                  "updateDatetime": null,
                  "customerGroup": null,
                  "customerType": null
                }
              ]
            }""";

    @Operation(operationId = "checkReasonFreeCam", summary = "Kiểm tra danh sách thiết bị CAM miễn phí theo gói sản phẩm (sale service), phục vụ getPriceInServices",
            description = "Trả về danh sách bản ghi FREE_CAM_EQUIPMENT khớp với gói sản phẩm (product package / sale service) truyền vào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHECK_REASON_FREE_CAM_EXAMPLE)))
    })
    @GetMapping("/checkReasonFreeCam/{productPackageId}")
    public StandardResponse<List<FreeCamEquipmentDTO>> checkReasonFreeCam(
            @Parameter(description = "Id gói sản phẩm (product package / sale service)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "productPackageId phải >= 0")
            @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
            Long productPackageId) {
        return StandardResponses.success(service.checkReasonFreeCam(productPackageId));
    }
}
