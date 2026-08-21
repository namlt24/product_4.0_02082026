package com.bccs.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Khai bao "trich xuat field tu response step X -> bom vao step Y".
 * Day chinh la co che chain du lieu giua cac backend trong composite API.
 *
 * Vi du: sourceStepOrder=1, sourceField="user_id", targetStepOrder=2,
 * targetType=QUERY, targetParamName="userId"
 * => step 2 se goi voi query string ...&userId={resp0_user_id}
 */
@Entity
@Table(name = "field_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "endpointConfig")
public class FieldMapping {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    @JsonIgnore
    private EndpointConfig endpointConfig;

    /** Step nguon (1-based) - noi field duoc trich xuat. */
    @Column(name = "source_step_order", nullable = false)
    private int sourceStepOrder;

    /** Ten field trong response JSON cua step nguon (ho tro dot-notation, vi du "data.id"). */
    @Column(name = "source_field", nullable = false)
    private String sourceField;

    /** Step dich (1-based) - noi gia tri duoc bom vao. Phai > sourceStepOrder. */
    @Column(name = "target_step_order", nullable = false)
    private int targetStepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private MappingTargetType targetType;

    /** Ten path token / query param / header o step dich. */
    @Column(name = "target_param_name", nullable = false)
    private String targetParamName;
}
