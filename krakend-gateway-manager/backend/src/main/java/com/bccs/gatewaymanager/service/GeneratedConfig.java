package com.bccs.gatewaymanager.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** Ket qua sinh cau hinh: JSON node + danh sach canh bao nghiep vu di kem. */
public record GeneratedConfig(JsonNode json, List<String> warnings) {
}
