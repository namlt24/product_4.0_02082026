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
 * stepOrder la so thu tu 1-based nhap tu UI; trong krakend.json, KrakenD
 * tham chieu response cua step theo index 0-based: resp0, resp1, resp2...
 * (tuc la respN voi N = stepOrder - 1).
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
     * Path template goi toi backend, vi du "/api/users/{id}".
     * - Token trung ten voi path-param cua endpoint gateway se duoc KrakenD
     *   tu dong forward (khong can cau hinh gi them).
     * - Token duoc khai bao trong FieldMapping (targetType=PATH) se duoc
     *   generator thay the bang placeholder {respN_field}.
     */
    @Column(name = "url_pattern", nullable = false)
    private String urlPattern;

    /** Danh sach host cua backend service, vi du ["http://auth-service:8081"]. */
    @ElementCollection
    @CollectionTable(name = "backend_step_host", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "host")
    @Builder.Default
    private List<String> hosts = new ArrayList<>();

    /**
     * Ten field can "boc vo" (unwrap) trong response truoc khi ap dung mapping/allow/deny/group
     * va truoc khi cac step sau co the trich xuat field qua {respN_field}. Rat huu ich voi cac
     * API BCCS vi StandardResponse luon boc du lieu that trong field "data"
     * (vi du: {"code":"SUCCESS","data":{"staffCode":"NV_001",...}} -> khai bao target="data"
     * de KrakenD chi giu lai noi dung cua "data" lam response goc cua backend nay).
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
