package com.bccs.gatewaymanager.entity;

/**
 * Noi mot truong duoc trich xuat tu response cua step truoc se duoc bom vao
 * o step sau: path param, query param, hoac header.
 *
 * Luu y quan trong (xem KrakendConfigGenerator): KrakenD Community Edition
 * chi ho tro native placeholder {respN_field} trong url_pattern (path + query).
 * HEADER khong duoc KrakenD CE ho tro native - se bi canh bao (warning) va
 * KHONG duoc ghi vao krakend.json khi deploy.
 */
public enum MappingTargetType {
    PATH, QUERY, HEADER
}
