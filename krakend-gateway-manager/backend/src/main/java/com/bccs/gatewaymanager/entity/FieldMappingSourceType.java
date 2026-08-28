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
    CONSTANT,

    /**
     * Gop TOAN BO field cua TUNG phan tu (moi phan tu la 1 object) trong 1 mang o
     * response step {@code sourceStepOrder} thanh 1 OBJECT DUY NHAT (union key/value) -
     * KHAC voi {@link #STEP_RESPONSE_ARRAY_AGGREGATE} (trich 1 field TEN CO DINH tu moi
     * phan tu thanh 1 MANG gia tri). Vi du: mang "data" gom N object 1-key rieng le
     * {"data":[{"500173047":"1"},{"400017940":"1"}]} -> gop thanh 1 object
     * {"500173047":"1","400017940":"1"}. Dung {@code sourceArrayField} (duong dan toi
     * mang) - KHONG dung sourceElementField (lay nguyen ca object, khong trich 1 field).
     * Key trung nhau giua cac phan tu: phan tu DEN SAU ghi de gia tri phan tu truoc.
     * Phan tu KHONG phai object bi bo qua (khong throw). CHI dung duoc voi
     * targetType=BODY_FIELD (object gop khong flatten duoc thanh chuoi cho PATH/QUERY/HEADER).
     */
    STEP_RESPONSE_ARRAY_MERGE
}
