package com.bccs.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mot backend service duoc goi trong chuoi (step) cua 1 endpoint gateway.
 * stepOrder la so thu tu 1-based nhap tu UI; CompositeOrchestratorEngine
 * thuc thi tuan tu theo dung thu tu nay tai request-time, cho phep step sau
 * tham chieu response cua step truoc qua FieldMapping (sourceType=STEP_RESPONSE).
 */
@Entity
@Table(name = "backend_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "endpointConfig")
public class BackendStep {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    @JsonIgnore
    private EndpointConfig endpointConfig;

    /** So thu tu step, bat dau tu 1 (Step 1, Step 2, ...). */
    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    /** Ten goi nho, vi du "Auth Service". */
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayMethod method;

    /**
     * Path template goi toi backend, vi du "/api/users/{id}" - CHI phan path,
     * host lay tu {@link #upstreamService}.
     * - Token trung ten voi path-param cua endpoint gateway se duoc engine tu
     *   dong forward (khong can cau hinh gi them).
     * - Token duoc khai bao trong FieldMapping (targetType=PATH) se duoc
     *   CompositeOrchestratorEngine thay the truc tiep tai request-time.
     */
    @Column(name = "url_pattern", nullable = false)
    private String urlPattern;

    /**
     * Backend that duoc goi o step nay - dang ky 1 lan qua UpstreamService,
     * mang theo host/timeout/circuit-breaker/cache. Thay the hoan toan danh
     * sach "hosts" go tay truoc day.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upstream_service_id", nullable = false)
    private UpstreamService upstreamService;

    /**
     * true = lay nguyen body goc cua client lam nen cho body gui di step nay,
     * roi moi ap cac FieldMapping targetType=BODY_FIELD de them/ghi de len tren.
     * Dung cho truong hop nhu search-isdn: body POST cuoi = body goc + truong moi.
     */
    @Builder.Default
    @Column(name = "forward_original_body", nullable = false)
    private boolean forwardOriginalBody = false;

    /**
     * Ten field can "boc vo" (unwrap) trong response truoc khi ap dung mapping/allow/deny/group
     * va truoc khi cac step sau co the trich xuat field qua FieldMapping. Rat huu ich voi cac
     * API BCCS vi StandardResponse luon boc du lieu that trong field "data"
     * (vi du: {"code":"SUCCESS","data":{"staffCode":"NV_001",...}} -> khai bao target="data"
     * de engine chi giu lai noi dung cua "data" lam response goc cua backend nay).
     */
    @Column(name = "target_field")
    private String target;

    /**
     * Neu set: goi response cua step nay se duoc long vao key nay khi merge, tranh dam field.
     * Cot DB dat ten "group_name" (khong phai "group") vi GROUP la tu khoa dung rieng
     * trong PostgreSQL - de nguyen "group" se gay loi syntax khi Hibernate tao bang.
     */
    @Column(name = "group_name")
    private String group;

    /** Whitelist field giu lai trong response cua step nay (loc bot du lieu). */
    @ElementCollection
    @CollectionTable(name = "backend_step_allow", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "field_name")
    @Builder.Default
    private List<String> allowFields = new ArrayList<>();

    /** Blacklist field loai bo khoi response cua step nay. */
    @ElementCollection
    @CollectionTable(name = "backend_step_deny", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "field_name")
    @Builder.Default
    private List<String> denyFields = new ArrayList<>();

    /** Doi ten field truoc khi merge, vi du {"id": "user_id"}. */
    @ElementCollection
    @CollectionTable(name = "backend_step_mapping", joinColumns = @JoinColumn(name = "step_id"))
    @MapKeyColumn(name = "source_field")
    @Column(name = "target_field")
    @Builder.Default
    private Map<String, String> fieldRenameMapping = new HashMap<>();
}
