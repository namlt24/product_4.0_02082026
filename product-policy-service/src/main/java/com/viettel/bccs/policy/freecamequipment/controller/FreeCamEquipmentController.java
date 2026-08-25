package com.viettel.bccs.policy.freecamequipment.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.freecamequipment.dto.response.FreeCamEquipmentDTO;
import com.viettel.bccs.policy.freecamequipment.openapi.ApiCheckReasonFreeCam;
import com.viettel.bccs.policy.freecamequipment.service.FreeCamEquipmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Free Cam Equipment", description = "APIs quản lý giá thiết bị CAM miễn phí theo lý do đấu nối")
@RestController
@RequestMapping("/product-policy-service/v1/free-cam-equipment")
@RequiredArgsConstructor
public class FreeCamEquipmentController {

    private final FreeCamEquipmentService service;

    @ApiCheckReasonFreeCam
    @GetMapping("/checkReasonFreeCam/{productPackageId}")
    public StandardResponse<List<FreeCamEquipmentDTO>> checkReasonFreeCam(
            @Parameter(description = "Id gói sản phẩm (product package / sale service)", example = "1", required = true)
            @PathVariable
            Long productPackageId) {
        return StandardResponses.success(service.checkReasonFreeCam(productPackageId));
    }
}
