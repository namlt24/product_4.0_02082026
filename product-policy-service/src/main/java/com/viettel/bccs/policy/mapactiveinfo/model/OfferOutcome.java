package com.viettel.bccs.policy.mapactiveinfo.model;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;

/**
 * Kết quả xử lý 1 {@code offerId} khi {@code MapActiveInfoValidateService.validateMapActiveInfo}
 * chạy song song vòng lặp offerIds (mỗi offerId 1 {@link java.util.concurrent.CompletableFuture})
 * - giữ lại offerId gốc để merge/báo lỗi đúng thứ tự list đầu vào, KHÔNG theo thứ tự hoàn thành.
 */
public record OfferOutcome(Long offerId, MapActiveInfoDTO mapActiveInfo, RuntimeException error) {
}
