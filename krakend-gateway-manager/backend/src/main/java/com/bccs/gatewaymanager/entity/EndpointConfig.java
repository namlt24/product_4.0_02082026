package com.bccs.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dinh nghia mot endpoint duoc expose boi KrakenD Gateway.
 *
 * - Neu chi co 1 step trong {@link #steps}: day la reverse-proxy don gian.
 * - Neu co nhieu step va {@link #sequential} = true: day la composite API,
 *   KrakenD se goi tuan tu tung backend, cho phep step sau tham chieu du lieu
 *   tu response cua step truoc thong qua {@link FieldMapping}.
 */
@Entity
@Table(name = "endpoint_config", uniqueConstraints = @UniqueConstraint(columnNames = "path"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"steps", "mappings"})
public class EndpointConfig {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /** Ten goi nho de hien thi tren UI, khong anh huong toi krakend.json. */
    @Column(nullable = false)
    private String name;

    private String description;

    /** Duong dan endpoint client se goi, vi du: /v1/user-orders */
    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayMethod method;

    /** true = sequential composite API (goi tuan tu nhieu backend + chain field). */
    @Builder.Default
    @Column(name = "is_sequential", nullable = false)
    private boolean sequential = false;

    @Builder.Default
    @Column(name = "output_encoding")
    private String outputEncoding = "json";

    /**
     * Bat idempotency-key: client gui header "Idempotency-Key" - lan goi dau tien voi 1 key
     * duoc thuc thi + cache response THANH CONG (khong cache loi, de client retry duoc sau
     * khi loi that da het) trong idempotencyTtlSeconds; lan goi TIEP THEO CUNG key trong TTL
     * do tra thang response da cache, KHONG goi lai engine (tranh side-effect lap, vi du
     * client tu dong retry 1 request POST co that tao du lieu). Mac dinh tat (false) - moi
     * endpoint hien co giu nguyen hanh vi cu 100% tru khi chu dong bat.
     */
    @Builder.Default
    @Column(name = "idempotency_enabled")
    private boolean idempotencyEnabled = false;

    @Builder.Default
    @Column(name = "idempotency_ttl_seconds")
    private int idempotencyTtlSeconds = 86400;

    /**
     * Chi co y nghia khi sequential=false (step DOC LAP - xem javadoc handle() cua
     * CompositeOrchestratorEngine). true = cac step doc lap duoc submit CHAY THAT SU
     * SONG SONG qua 1 thread pool rieng (parallelStepExecutor) thay vi vong lap Java
     * tuan tu nhu truoc - giam do tre tong khi cac step khong phu thuoc du lieu lan
     * nhau (khac han sequential=true, noi step SAU luon can du lieu step TRUOC).
     *
     * DANH DOI PHAI BIET (canh bao ro tren UI): khi bat, TAT CA step da duoc submit
     * truoc khi biet step nao loi - 1 step co side-effect that (POST/PUT/DELETE) co
     * the da CHAY XONG truoc khi loi cua step khac duoc phat hien, khac han vong lap
     * tuan tu (loi o dau dung ngay o do, step sau khong bao gio chay). Mac dinh tat
     * (false) - moi endpoint non-sequential hien co giu nguyen vong lap tuan tu cu,
     * khong doi hanh vi/thu tu gi ca.
     */
    @Builder.Default
    @Column(name = "parallel_execution")
    private boolean parallelExecution = false;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "endpointConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<BackendStep> steps = new ArrayList<>();

    // @OrderBy("mappingOrder ASC") - dung de trang "Khai bao endpoint keo tha" doc/luu thu tu
    // on dinh; thu tu nay KHONG anh huong CompositeOrchestratorEngine (ap tat ca mapping cua
    // 1 step cung luc, khong tuan tu). Truoc khi them dong nay, thu tu doc von da khong xac
    // dinh/tuy DB nen day khong phai thay doi hanh vi man hinh cu dang phu thuoc vao.
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "endpointConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("mappingOrder ASC")
    private List<FieldMapping> mappings = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // --- helper de giu quan he 2 chieu nhat quan khi build tu DTO ---

    public void addStep(BackendStep step) {
        step.setEndpointConfig(this);
        this.steps.add(step);
    }

    public void addMapping(FieldMapping mapping) {
        mapping.setEndpointConfig(this);
        this.mappings.add(mapping);
    }

    public void replaceSteps(List<BackendStep> newSteps) {
        this.steps.clear();
        newSteps.forEach(this::addStep);
    }

    public void replaceMappings(List<FieldMapping> newMappings) {
        this.mappings.clear();
        newMappings.forEach(this::addMapping);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointConfig other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
