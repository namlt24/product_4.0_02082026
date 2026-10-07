package com.bccs.gatewaymanager.entity;

/**
 * Loai van de phat hien tren Data Plane. Them gia tri moi (vd HEALTH_CHECK_DOWN
 * phase 3) khong can migration - cot ALERT_TYPE la VARCHAR2 khong CHECK.
 */
public enum AlertType {
    /** CircuitBreaker cua 1 upstream chuyen sang OPEN - traffic bi fail-fast 503. */
    CIRCUIT_OPEN,
    /** Ty le loi (5xx/timeout) trong cua so thoi gian vuot nguong. */
    ERROR_RATE_HIGH,
    /** Bulkhead tu choi nhieu lenh goi trong cua so - nguy co 503 GW-BULKHEAD-FULL. */
    BULKHEAD_SATURATED,
    /** 1 endpoint cu the co ty le request loi vuot nguong (danh cho endpoint co nhieu step hop don loi tu nhieu nguon). */
    ENDPOINT_ERROR_RATE
}
