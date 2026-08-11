package com.viettel.bccs.policy.discountpromotion.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "DiscountPromotion", description = "APIs quản lý khuyến mãi giảm giá")
@RestController
@RequestMapping("/product-policy-service/v1/discountpromotion")
@RequiredArgsConstructor
@Validated
public class DiscountPromotionController {

    private final DiscountPromotionService service;

    private static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "discountPromotionId": 1,
                "telecomServiceId": "100",
                "code": "KM001",
                "name": "Khuyến mãi tháng 8",
                "type": "1",
                "systemType": "1",
                "discountMethod": "1",
                "discountPolicy": "POL001",
                "subType": "1",
                "monthCommitment": 12,
                "pricePlan": "GOI001",
                "monthAmount": 50000,
                "status": "1",
                "description": null,
                "content": null,
                "areaCode": null,
                "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "createUser": "system",
                "createDatetime": "2026-08-01T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "cycle": 1,
                "listType": null,
                "subListId": null,
                "note": null
              }
            }""";

    private static final String PROMOTION_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "discountPromotionId": 1,
                  "telecomServiceId": "100",
                  "code": "KM001",
                  "name": "Khuyến mãi tháng 8",
                  "type": "1",
                  "systemType": "1",
                  "discountMethod": "1",
                  "discountPolicy": "POL001",
                  "subType": "1",
                  "monthCommitment": 12,
                  "pricePlan": "GOI001",
                  "monthAmount": 50000,
                  "status": "1",
                  "description": null,
                  "content": null,
                  "areaCode": null,
                  "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "createUser": "system",
                  "createDatetime": "2026-08-01T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "cycle": 1,
                  "listType": null,
                  "subListId": null,
                  "note": null,
                  "regReasonId": null,
                  "reasonName": null,
                  "subGroupCode": null,
                  "productCode": null
                }
              ]
            }""";

    @Operation(operationId = "findDiscountPromotionById", summary = "Lấy khuyến mãi giảm giá theo id",
            description = "Tra cứu 1 bản ghi DISCOUNT_PROMOTION theo DISCOUNT_PROMOTION_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FIND_BY_ID_EXAMPLE)))
    })
    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionResponse> findById(
            @Parameter(description = "Id khuyến mãi giảm giá (DISCOUNT_PROMOTION_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "id phải >= 0")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @Operation(operationId = "getPromotionList", summary = "Lấy danh sách khuyến mãi giảm giá",
            description = "Tra cứu danh sách DISCOUNT_PROMOTION theo dịch vụ viễn thông, có thể lọc theo trạng thái và ngày hiệu lực.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PROMOTION_LIST_EXAMPLE)))
    })
    @GetMapping("/getPromotionList")
    public StandardResponse<List<DiscountPromotionDTO>> getPromotionList(
            @Parameter(description = "Id dịch vụ viễn thông", example = "100")
            @RequestParam(required = false)
            @Min(value = 0, message = "telecomServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
            Long telecomServiceId,
            @Parameter(description = "Có kiểm tra trạng thái hay không", example = "false")
            @RequestParam(defaultValue = "false") boolean checkStatus,
            @Parameter(description = "Có kiểm tra ngày hiệu lực hay không", example = "false")
            @RequestParam(defaultValue = "false") boolean checkEffectDate,
            @Parameter(description = "Ngày hết hiệu lực để so sánh", example = "2026-08-11T02:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return StandardResponses.success(
                service.getPromotionList(telecomServiceId, checkStatus, checkEffectDate, endDate));
    }
}
