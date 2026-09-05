package com.bccs.gatewaymanager.dto;

import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * Ket qua 1 lan "Thu nhanh" (xem EndpointTryService) - envelope tra ve LUON
 * HTTP 200 cho ca 2 endpoint try (`/{id}/try` va `/try-adhoc`), ke ca khi
 * that bai: day la cong cu debug o Control Plane, khong phai hop dong API
 * cho client that, nen chap nhan lech chuan REST status de frontend xu ly 1
 * luong duy nhat (khong tach nhanh success/error qua HTTP status) - vua nhan
 * duoc `result` VUA nhan duoc `hops` (waterfall tung step) trong CUNG 1 response.
 *
 * Loi khi GOI SAI API (id khong ton tai, body khong phai JSON hop le) VAN
 * throw binh thuong qua GlobalExceptionHandler nhu truoc - chi loi xay ra
 * TRONG luc validate draft/thuc thi engine moi duoc boc vao day.
 */
public record TryResultDto(
        boolean success,
        JsonNode result,
        String errorCode,
        String errorMessage,
        List<StepTraceDto> hops
) {
}
