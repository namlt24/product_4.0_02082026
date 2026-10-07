package com.bccs.gatewaymanager.entity;

/**
 * Muc do nghiem trong cua canh bao - chi dung de to mau + sap xep tren UI,
 * khong tham gia logic dedup (dedup key khong gom severity vi 1 van de khong
 * the doi do nghiem trong khi van la cung 1 van de).
 */
public enum AlertSeverity {
    /** Traffic bi chan hoac toan bo (CB OPEN, upstream chet hoan toan). */
    CRITICAL,
    /** Van de dang thay nguyen nang (error rate cao, bulkhead day, latency). */
    WARNING
}
