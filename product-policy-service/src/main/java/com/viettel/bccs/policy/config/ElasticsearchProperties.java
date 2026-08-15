package com.viettel.bccs.policy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình kết nối Elasticsearch cho product-policy-service (dùng cho search MAP_ACTIVE_INFO —
 * xem {@link com.viettel.bccs.policy.mapactiveinfo.elasticsearch.MapActiveInfoElasticsearchService}).
 *
 * <p>Trước đây đóng thành module riêng {@code bccs-starter-elasticsearch} (mô phỏng 1 BCCS starter
 * thật, cài qua {@code .m2} local) — nay gộp thẳng vào service theo quyết định mới, không cần build/
 * cài module riêng nữa. Elasticsearch chưa có starter thật trên Nexus nên đây là giải pháp tạm; khi
 * nào {@code bccs-starter-elasticsearch} thật được publish, có thể quay lại dùng starter đó.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "bccs.elasticsearch")
public class ElasticsearchProperties {

    /** Bật/tắt cấu hình kết nối Elasticsearch. Mặc định tắt để không ảnh hưởng môi trường chưa cần ES. */
    private boolean enabled = false;

    /** Danh sách host:port của cụm Elasticsearch. VD local: ["localhost:9200"]. */
    private List<String> hosts = new ArrayList<>(List.of("localhost:9200"));

    /** Scheme kết nối: http (mặc định local) hoặc https (cụm thật có bật TLS). */
    private String scheme = "http";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration socketTimeout = Duration.ofSeconds(30);

    /** ACL Authentication — để trống khi cụm không bật security (VD local dev). */
    private String username;

    private String password;
}
