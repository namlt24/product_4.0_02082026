package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.exception.SystemException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Ghi krakend.json ra volume dung chung voi container KrakenD.
 *
 * Ghi ATOMIC: viet ra file .tmp truoc, roi Files.move() voi ATOMIC_MOVE.
 * Muc dich: KrakenD (hoac bat ky process nao dang doc file nay) khong bao
 * gio thay mot file JSON dang ghi do (partial write) - tranh loi parse config
 * khi container restart dung luc backend dang ghi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigFileWriterService {

    private final ObjectMapper objectMapper;

    @Value("${gatewaymanager.krakend.config-path:/etc/krakend/krakend.json}")
    private String configPath;

    public void write(JsonNode json) {
        try {
            Path target = Path.of(configPath);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = target.resolveSibling(target.getFileName().toString() + ".tmp");

            String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            Files.writeString(tmp, pretty, StandardCharsets.UTF_8);

            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.info("Da ghi cau hinh KrakenD (atomic) tai {}", target.toAbsolutePath());
        } catch (IOException e) {
            throw new SystemException("Khong the ghi file cau hinh KrakenD: " + e.getMessage(), e);
        }
    }
}
