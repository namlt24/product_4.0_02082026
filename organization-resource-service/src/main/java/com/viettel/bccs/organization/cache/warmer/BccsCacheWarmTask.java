package com.viettel.bccs.organization.cache.warmer;

import java.time.Duration;

/**
 * Khai báo 1 tác vụ "làm nóng" cache: cache nào, chu kỳ bao lâu, và cách warm (thường là gọi lại
 * chính method đã {@code @Cacheable} cho tập key đã biết trước để Spring ghi đè cache với TTL mới).
 * Mỗi bean implement interface này được {@link BccsCacheWarmerScheduler} tự động phát hiện và chạy
 * — thêm 1 cache mới cần warm chỉ cần thêm 1 bean nhỏ implement interface này, không cần sửa
 * scheduler dùng chung.
 */
public interface BccsCacheWarmTask {

    /** Tên cache, dùng để log và theo dõi lần chạy gần nhất của từng task. */
    String cacheName();

    /** Chu kỳ warm mong muốn cho cache này (nên ngắn hơn TTL L1 của cache để không bao giờ hết hạn thật sự). */
    Duration interval();

    /** Thực thi warm: lấy tập key cần warm rồi gọi lại (các) method {@code @Cacheable} tương ứng. */
    void warm();
}
