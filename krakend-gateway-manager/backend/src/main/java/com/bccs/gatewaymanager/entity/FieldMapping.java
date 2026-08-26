package com.bccs.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

/**
 * Khai bao "trich xuat 1 gia tri -> bom vao step Y". Day la co che chain du
 * lieu giua cac backend trong composite API, ho tro 3 nguon (xem
 * {@link FieldMappingSourceType}): tu response step truoc (hanh vi cu), tu
 * chinh body cua client, hoac gop mang.
 *
 * Vi du 1 (hanh vi cu): sourceType=STEP_RESPONSE, sourceStepOrder=1,
 * sourceField="user_id", targetStepOrder=2, targetType=QUERY, targetParamName="userId".
 *
 * Vi du 2 (moi - doc tu body client): sourceType=REQUEST_BODY,
 * sourceField="telecomServiceId", targetStepOrder=1, targetType=QUERY,
 * targetParamName="telecomServiceId".
 *
 * Vi du 3 (moi - gop mang): sourceType=STEP_RESPONSE_ARRAY_AGGREGATE,
 * sourceStepOrder=1, sourceArrayField="data", sourceElementField="code",
 * targetStepOrder=2, targetType=BODY_FIELD, targetParamName="prodOfferCodeLst".
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

    /**
     * Nguon du lieu: tu response step truoc (mac dinh, hanh vi cu), tu chinh
     * body cua client, hoac gop 1 field cua tung phan tu trong 1 mang thanh
     * mang moi. Xem {@link FieldMappingSourceType}.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private FieldMappingSourceType sourceType = FieldMappingSourceType.STEP_RESPONSE;

    /** Step nguon (1-based) - noi field duoc trich xuat. Bo qua khi sourceType=REQUEST_BODY. */
    @Column(name = "source_step_order")
    private Integer sourceStepOrder;

    /**
     * sourceType=STEP_RESPONSE: ten field trong response JSON cua step nguon (ho tro dot-notation, vi du "data.id").
     * sourceType=REQUEST_BODY: duong dan toi field trong body goc cua client (vi du "telecomServiceId").
     * sourceType=STEP_RESPONSE_ARRAY_AGGREGATE: khong dung, xem sourceArrayField/sourceElementField.
     */
    @Column(name = "source_field")
    private String sourceField;

    /** sourceType=STEP_RESPONSE_ARRAY_AGGREGATE: duong dan toi mang trong response step nguon, vi du "data". */
    @Column(name = "source_array_field")
    private String sourceArrayField;

    /** sourceType=STEP_RESPONSE_ARRAY_AGGREGATE: ten field lay tu MOI phan tu cua mang, vi du "code". */
    @Column(name = "source_element_field")
    private String sourceElementField;

    /** Step dich (1-based) - noi gia tri duoc bom vao. Phai > sourceStepOrder khi sourceType=STEP_RESPONSE*. */
    @Column(name = "target_step_order", nullable = false)
    private int targetStepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private MappingTargetType targetType;

    /** Ten path token / query param / header o step dich. */
    @Column(name = "target_param_name", nullable = false)
    private String targetParamName;

    /**
     * Vi tri hien thi khi sap xep (trang "Khai bao endpoint keo tha") - KHONG
     * anh huong hanh vi engine (CompositeOrchestratorEngine ap tat ca mapping
     * cua 1 step cung luc, khong tuan tu theo thu tu nay). Bang field_mapping
     * da co du lieu that nen can @ColumnDefault de Hibernate sinh DDL kem
     * DEFAULT khi ALTER TABLE them cot (dung mau da verify voi BackendStep.cacheEnabled).
     */
    @Builder.Default
    @Column(name = "mapping_order", nullable = false)
    @ColumnDefault("0")
    private int mappingOrder = 0;
}
