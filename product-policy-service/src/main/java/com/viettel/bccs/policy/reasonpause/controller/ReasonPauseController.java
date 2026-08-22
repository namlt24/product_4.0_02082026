package com.viettel.bccs.policy.reasonpause.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
import com.viettel.bccs.policy.utils.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.viettel.bccs.policy.reasonpause.openapi.ReasonPauseControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/reason-pause")
@RequiredArgsConstructor
public class ReasonPauseController {

    private final ReasonPauseService service;

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
            Long id) {
        RequestValidator.checkRange(id, "id", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
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
            Long reasonId) {
        RequestValidator.checkRange(reasonId, "reasonId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        return StandardResponses.success(service.getReasonPauseByReasonId(reasonId));
    }
}
