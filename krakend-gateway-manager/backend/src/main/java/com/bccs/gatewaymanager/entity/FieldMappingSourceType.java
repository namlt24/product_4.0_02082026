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

    /**
     * Gop 1 field cua TUNG phan tu trong 1 mang o response step {@code sourceStepOrder}
     * thanh 1 mang moi. Vi du: mang "data" gom cac object co field "code"
     * -> gop thanh ["code1","code2",...]. Dung {@code sourceArrayField} (duong dan
     * toi mang) va {@code sourceElementField} (ten field lay tu moi phan tu).
     */
    STEP_RESPONSE_ARRAY_AGGREGATE
}
