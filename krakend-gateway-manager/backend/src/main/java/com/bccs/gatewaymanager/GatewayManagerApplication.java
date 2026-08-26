package com.bccs.gatewaymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dynamic Composite API Orchestrator - vua la Control Plane (CRUD dinh nghia
 * Endpoint/Upstream Service qua /api/**) vua CHINH LA gateway thuc thi traffic
 * that (DynamicDispatcherController + CompositeOrchestratorEngine, xem 2 lop
 * do de hieu luong xu ly 1 request). Khong con phu thuoc KrakenD/Gravitee hay
 * bat ky gateway ben thu 3 nao - toan bo composition duoc thuc thi truc tiep
 * trong chinh Spring Boot service nay tai request-time.
 */
@SpringBootApplication
public class GatewayManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayManagerApplication.class, args);
    }
}
