package com.bccs.gatewaymanager.entity;

/**
 * Noi 1 gia tri (tu step truoc hoac tu body client) duoc bom vao khi goi step sau:
 * path param, query param, header, hoac 1 field trong JSON body gui di.
 *
 * Lich su: truoc day (khi con dung KrakenD lam runtime) HEADER khong duoc
 * KrakenD CE ho tro native. Gio engine tu thuc thi (xem CompositeOrchestratorEngine)
 * nen ca 4 loai deu duoc ho tro day du, khong con gioi han nao tu bien thu 3.
 */
public enum MappingTargetType {
    PATH, QUERY, HEADER,

    /**
     * Set/them 1 field vao JSON body sap gui di cua step dich (dung targetParamName
     * lam ten field). Day la co che thay the json-to-json/Groovy cua Gravitee -
     * chinh xac thu can de gop mang prodOfferCodeLst vao body goc truoc khi goi
     * backend cuoi.
     */
    BODY_FIELD
}
