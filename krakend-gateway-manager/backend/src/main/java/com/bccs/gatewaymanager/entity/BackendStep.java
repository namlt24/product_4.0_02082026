package com.bccs.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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
     * mang theo host/timeout/circuit-breaker. Thay the hoan toan danh
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
     * Cache Redis (cache-aside) cho step nay - CHI ap dung khi method=GET (xem
     * UpstreamHttpExecutor.call()). Dat o cap step (khong phai cap UpstreamService)
     * vi 1 Upstream bi nhieu step goi toi nhieu ham/path khac nhau, khong phai
     * ham nao cung nen cache (vi du ham tra du lieu doi lien tuc khong nen cache
     * du dung chung Upstream voi 1 ham khac tra du lieu tinh).
     */
    @Builder.Default
    @Column(name = "cache_enabled", nullable = false)
    @ColumnDefault("false")
    private boolean cacheEnabled = false;

    /**
     * TTL cache (giay), chi co y nghia khi cacheEnabled=true. @ColumnDefault de
     * Hibernate sinh DDL kem DEFAULT khi ALTER TABLE them cot vao bang da co du
     * lieu (Oracle tu choi them cot NOT NULL khong DEFAULT vao bang khong rong).
     */
    @Builder.Default
    @Column(name = "cache_ttl_seconds", nullable = false)
    @ColumnDefault("300")
    private int cacheTtlSeconds = 300;

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

    /**
     * Vi tri X/Y tren canvas "khai bao endpoint keo tha" (trang endpoint-canvas) -
     * null = chua tung duoc keo tha thu cong, frontend tu suy auto-layout theo
     * stepOrder. Nullable, KHONG can @ColumnDefault (khac cacheEnabled/cacheTtlSeconds
     * o tren - 2 cot nay KHONG NOT NULL nen Hibernate ALTER TABLE them cot vao bang
     * da co du lieu luon an toan, khong can gia tri mac dinh).
     */
    @Column(name = "canvas_x")
    private Integer canvasX;

    @Column(name = "canvas_y")
    private Integer canvasY;

    /**
     * Override connectTimeout/readTimeout cua UpstreamService RIENG cho step nay -
     * null = dung dung mac dinh cua UpstreamService (hanh vi cu, khong doi). Dat o
     * cap step (khong phai UpstreamService) vi cung 1 Upstream co the co ham nhanh
     * (lookup don gian) va ham cham (report/tong hop) khac han nhau - dung chung 1
     * timeout cho ca 2 la khong hop ly (xem UpstreamHttpExecutor.restTemplateFor()).
     * Nullable, KHONG can @ColumnDefault (giong canvasX/canvasY o tren).
     */
    @Column(name = "connect_timeout_ms")
    private Integer connectTimeoutMs;

    @Column(name = "read_timeout_ms")
    private Integer readTimeoutMs;

    /**
     * Re nhanh (P1-5): sau khi step nay chay xong, dua vao 1 dieu kien (so sanh
     * 1 field lay tu response step truoc/body client) de quyet dinh step TIEP
     * THEO SE GOI la step nao - KHAC voi hanh vi mac dinh (luon di theo stepOrder
     * ke tiep). Tat ca nullable, KHONG can @ColumnDefault (giong canvasX/canvasY
     * o tren) - step khong khai bao dieu kien (conditionOperator=null) van chay
     * dung 100% hanh vi cu (xem CompositeOrchestratorEngine.determineNextStepOrder()).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_source_type")
    private FieldMappingSourceType conditionSourceType;

    @Column(name = "condition_source_step_order")
    private Integer conditionSourceStepOrder;

    @Column(name = "condition_source_field")
    private String conditionSourceField;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator")
    private ConditionOperator conditionOperator;

    @Column(name = "condition_expected_value")
    private String conditionExpectedValue;

    /** null = neu dieu kien DUNG thi ket thuc chuoi tai day (ket qua step nay la response cuoi cung). */
    @Column(name = "next_step_order_if_true")
    private Integer nextStepOrderIfTrue;

    /** null = neu dieu kien SAI thi ket thuc chuoi tai day. */
    @Column(name = "next_step_order_if_false")
    private Integer nextStepOrderIfFalse;

    /**
     * Fallback khi step nay LOI (upstream timeout/4xx/5xx...) - thay vi throw ngay va ket
     * thuc ca chuoi (hanh vi mac dinh, van giu nguyen khi null), nhay sang step nay tiep
     * tuc chay. DOC LAP voi conditionOperator o tren (dung duoc ca cho step tuan tu thuong,
     * khong bat buoc dang re nhanh) - dung LAI CO CHE "graph pointer" da co cua rieng nhanh,
     * di qua dung executedOrders cycle-guard trong CompositeOrchestratorEngine.executeSequentialChain()
     * nen khong can code chong vong lap rieng. Nullable, KHONG can @ColumnDefault (giong
     * canvasX/canvasY o tren) - step khong khai bao (null) van throw dung 100% hanh vi cu.
     */
    @Column(name = "on_error_step_order")
    private Integer onErrorStepOrder;
}
