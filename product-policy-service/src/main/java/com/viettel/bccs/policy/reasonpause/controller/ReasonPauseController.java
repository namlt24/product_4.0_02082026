package com.viettel.bccs.policy.reasonpause.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/reason-pause")
@RequiredArgsConstructor
@Validated
public class ReasonPauseController {

    private final ReasonPauseService service;

    private static final String REASON_PAUSE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "reasonPauseId": 1,
                "numMonth": 3,
                "price": 50000,
                "reasonId": 1,
                "status": "1",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "createUser": "system",
                "updateDatetime": null,
                "updateUser": null
              }
            }""";

    private static final String REASON_PAUSE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "reasonPauseId": 1,
                  "numMonth": 3,
                  "price": 50000,
                  "reasonId": 1,
                  "status": "1",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "createUser": "system",
                  "updateDatetime": null,
                  "updateUser": null
                }
              ]
            }""";

    @GetMapping("/findById/{id}")
    @Operation(operationId = "findReasonPauseById", summary = "Lấy kỳ tạm ngưng theo ID",
            description = "Tra cứu 1 bản ghi REASON_PAUSE theo REASON_PAUSE_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_PAUSE_SINGLE_EXAMPLE)))
    })
    public StandardResponse<ReasonPauseDTO> findById(
            @Parameter(description = "ID kỳ tạm ngưng (REASON_PAUSE_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "id phải >= 0")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getReasonPauseByReasonId/{reasonId}")
    @Operation(operationId = "getReasonPauseByReasonId", summary = "Lấy danh sách kỳ tạm ngưng theo ID hình thức hòa mạng",
            description = "Tra cứu các bản ghi REASON_PAUSE theo REASON_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_PAUSE_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ReasonPauseDTO>> getReasonPauseByReasonId(
            @Parameter(description = "ID hình thức hòa mạng (REASON_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "reasonId phải >= 0")
            @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
            Long reasonId) {
        return StandardResponses.success(service.getReasonPauseByReasonId(reasonId));
    }
}
