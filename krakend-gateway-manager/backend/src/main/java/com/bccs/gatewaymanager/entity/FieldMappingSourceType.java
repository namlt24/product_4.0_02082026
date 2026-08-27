package com.bccs.gatewaymanager.entity;

/**
 * Nguon du lieu cua 1 FieldMapping - day la kha nang cot loi ma KrakenD/Gravitee
 * khong lam duoc "thuan cau hinh": doc field tu chinh BODY cua client (khong
 * phai tu response cua step truoc), va gop 1 field cua tung phan tu trong 1
 * mang response thanh 1 mang moi.
 */
public enum FieldMappingSourceType {
    /** Mac dinh - lay tu response cua step {@code sourceStepOrder} (hanh vi cu). */
    STEP_RESPONSE,

    /** Lay tu chinh body goc cua client gui len (sourceStepOrder bi bo qua). */
    REQUEST_BODY,

    /** Lay tu chinh query param cua client gui len (sourceStepOrder bi bo qua, giong REQUEST_BODY). */
    QUERY_PARAM,

    /**
     * Gop 1 field cua TUNG phan tu trong 1 mang o response step {@code sourceStepOrder}
     * thanh 1 mang moi. Vi du: mang "data" gom cac object co field "code"
     * -> gop thanh ["code1","code2",...]. Dung {@code sourceArrayField} (duong dan
     * toi mang) va {@code sourceElementField} (ten field lay tu moi phan tu).
     */
    STEP_RESPONSE_ARRAY_AGGREGATE,

    /**
     * Gia tri HANG SO co dinh, khai bao truc tiep trong cau hinh (khong doc tu request/
     * response nao ca) - dung {@code constantValue}, bo qua sourceStepOrder/sourceField.
     * Vi du: 2 nhanh re (P1-5) cung goi 1 API X nhung fix cung khac nhau field "priority"
     * ("low" vs "high") khong phu thuoc client gui gi. Voi targetType=BODY_FIELD,
     * constantValue duoc thu parse nhu JSON truoc (ho tro so/boolean/object/mang -
     * vi du constantValue="3" thanh so JSON 3, "true" thanh boolean) - parse loi thi
     * fallback ve chuoi text nguyen ban (vi du "low" khong phai JSON hop le -> giu
     * chuoi "low"). Voi PATH/QUERY/HEADER thi luon la chuoi text nguyen ban.
     */
    CONSTANT
}
