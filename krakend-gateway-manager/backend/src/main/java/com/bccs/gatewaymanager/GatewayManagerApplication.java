package com.bccs.gatewaymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Control Plane API cho KrakenD API Gateway.
 *
 * Ung dung nay KHONG phai la gateway - no la mot Spring Boot service quan ly
 * (CRUD) dinh nghia endpoint, sinh ra file krakend.json chuan format KrakenD v3
 * va trigger reload container KrakenD chay rieng qua Docker Engine API.
 */
@SpringBootApplication
public class GatewayManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayManagerApplication.class, args);
    }
}
