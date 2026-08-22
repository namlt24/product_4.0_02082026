package com.viettel.bccs.organization.channeltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import com.viettel.bccs.organization.utils.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.viettel.bccs.organization.channeltype.openapi.ChannelTypeControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/channel-type")
@RequiredArgsConstructor
@Tag(name = "ChannelType", description = "Tra cứu loại kênh (CHANNEL_TYPE)")
public class ChannelTypeController {

    private final ChannelTypeService channelTypeService;

    @Operation(operationId = "getActiveChannelTypeById", summary = "Lấy loại kênh đang hiệu lực theo ID",
            description = "Tra cứu 1 bản ghi CHANNEL_TYPE đang hiệu lực (status = 1) theo CHANNEL_TYPE_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHANNEL_TYPE_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy loại kênh với ID tương ứng")
    })
    @GetMapping("/getActiveById/{channelTypeId}")
    public StandardResponse<ChannelTypeDTO> getActiveById(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @PathVariable
            Long channelTypeId) {
        RequestValidator.checkRange(channelTypeId, "channelTypeId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        return StandardResponses.success(channelTypeService.getActiveById(channelTypeId));
    }

}
