package com.bccs.gatewaymanager.entity;

/**
 * Trang thai vong doi 1 canh bao - ACTIVE la trang thai "dang van de",
 * doi tu Data Plane touch (update LAST_SEEN_AT) lien tuc; khi DP khong con
 * day touch (van de het) hoac qua han staleness thi CP tu chuyen RESOLVED.
 */
public enum AlertStatus {
    /** Dang phat sinh - hien mau do/vang tren UI, tinh vao badge toolbar. */
    ACTIVE,
    /** Van con dang phat sinh nhung nguoi van hanh da xac nhan da biet. */
    ACKNOWLEDGED,
    /** Da ket thuc (DP thong bao resolved=true hoac CP qua han staleness). */
    RESOLVED
}
