package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.service.EndpointService;
import com.bccs.gatewaymanager.service.UpstreamServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Seed 2 Upstream Service + 1 endpoint composite mau ("/v1/user-orders") khi DB
 * con trong, de nguoi dung thay ngay vi du thuc te ma khong phai tu tay nhap khi
 * lan dau chay `docker compose up -d`. Chi chay 1 lan - neu da co du lieu thi bo qua.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EndpointConfigRepository repository;
    private final EndpointService endpointService;
    private final UpstreamServiceService upstreamServiceService;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("DB da co {} endpoint - bo qua seed du lieu mau.", repository.count());
            return;
        }

        UpstreamServiceDto authUpstream = upstreamServiceService.create(new UpstreamServiceDto(
                null, "auth-service", "Demo - dich vu xac thuc/user",
                "http://auth-service:8081", 1000, 3000, true, 50, true, null, null));

        UpstreamServiceDto orderUpstream = upstreamServiceService.create(new UpstreamServiceDto(
                null, "order-service", "Demo - dich vu don hang",
                "http://order-service:8082", 1000, 3000, true, 50, true, null, null));

        // Step 1: goi Auth Service lay thong tin user hien tai -> co field "id".
        // Demo cache Redis BAT o day (TTL 60s) - thong tin user it doi trong thoi gian ngan
        // nen hop ly de cache; step 2 (don hang) giu TAT vi du lieu doi lien tuc hon.
        BackendStepDto step1 = new BackendStepDto(
                null, 1, "Auth Service - lay thong tin user", GatewayMethod.GET,
                "/api/v1/users/{userId}",
                authUpstream.id(), authUpstream.name(),
                false,
                true, 60,                     // cacheEnabled, cacheTtlSeconds - demo cache theo tung step
                "auth",                       // group: tranh dam field khi merge voi step 2
                null,                         // target: khong can boc vo, response Auth Service khong bi wrap
                List.of("id", "name", "email"), // allow: chi giu 3 field can thiet
                List.of(),
                Map.of("id", "user_id")        // rename "id" -> "user_id" cho ro nghia truoc khi chain
        );

        // Step 2: goi Order Service, dung user_id trich xuat tu step 1 lam query param
        BackendStepDto step2 = new BackendStepDto(
                null, 2, "Order Service - lay danh sach don hang", GatewayMethod.GET,
                "/api/v1/orders",
                orderUpstream.id(), orderUpstream.name(),
                false,
                false, 300,                   // cacheEnabled, cacheTtlSeconds - tat vi don hang doi lien tuc
                null,
                null,                         // target: khong can boc vo
                List.of(),
                List.of("internal_debug_info"), // deny: loai field noi bo truoc khi tra ve client
                Map.of()
        );

        // Chain: field "user_id" tu response step 1 -> query param "userId" cua step 2
        FieldMappingDto chain = new FieldMappingDto(
                null, FieldMappingSourceType.STEP_RESPONSE, 1, "user_id", null, null,
                2, MappingTargetType.QUERY, "userId", 0);

        // Path endpoint PHAI khai bao {userId} vi Step 1 dung token nay trong url_pattern -
        // engine chi tu dong forward path-param khi ten token khop giua endpoint va backend.
        EndpointRequestDto request = new EndpointRequestDto(
                "User Orders (composite)",
                "Vi du composite API: goi tuan tu Auth Service -> Order Service, "
                        + "truyen userId trich xuat tu response Auth sang query cua Order.",
                "/v1/user-orders/{userId}",
                GatewayMethod.GET,
                true,
                "json",
                List.of(step1, step2),
                List.of(chain)
        );

        endpointService.create(request);
        log.info("Da seed endpoint mau: GET /v1/user-orders (composite, sequential).");
    }
}
