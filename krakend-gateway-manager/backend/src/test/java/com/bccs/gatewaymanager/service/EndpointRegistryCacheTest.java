package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Router 2 tang (finding #1 tu senior review): da so endpoint la path "tinh"
 * (khong {param}) nen tra O(1) qua findExact()/exactIndex, chi endpoint co
 * {param} moi roi vao patternEndpoints() (danh sach nho hon "all()", con lai
 * cho DynamicDispatcherController tu quet PathPattern.matches()).
 *
 * TU 2026-09: class nay khong con phu thuoc JPA (xem javadoc
 * EndpointRegistryCache) - reload() nhan thang List<EndpointResponseDto>,
 * khong can mock repository/mapper/TransactionManager nua.
 */
class EndpointRegistryCacheTest {

    private EndpointRegistryCache cache;

    @BeforeEach
    void setUp() {
        cache = new EndpointRegistryCache();
    }

    @Test
    void reload_pathTinh_vaoExactIndex_traO1() {
        EndpointResponseDto dto = dto("e1", "/v1/foo", GatewayMethod.GET);

        cache.reload(List.of(dto));

        assertThat(cache.findExact("GET", "/v1/foo")).isEqualTo(dto);
        assertThat(cache.patternEndpoints()).isEmpty();
        assertThat(cache.all()).containsExactly(dto);
    }

    @Test
    void reload_pathCoParam_vaoPatternEndpoints_khongVaoExactIndex() {
        EndpointResponseDto dto = dto("e2", "/v1/staff/{staffId}", GatewayMethod.GET);

        cache.reload(List.of(dto));

        // exactIndex la tra cuu CHUOI-DUNG-CHUOI, khong tu suy pattern - endpoint
        // co {param} PHAI di qua patternEndpoints()/PathPattern o dispatcher.
        assertThat(cache.findExact("GET", "/v1/staff/{staffId}")).isNull();
        assertThat(cache.findExact("GET", "/v1/staff/123")).isNull();
        assertThat(cache.patternEndpoints()).containsExactly(dto);
    }

    @Test
    void findExact_saiMethod_traNull() {
        EndpointResponseDto dto = dto("e1", "/v1/foo", GatewayMethod.GET);

        cache.reload(List.of(dto));

        assertThat(cache.findExact("POST", "/v1/foo")).isNull();
    }

    @Test
    void reload_tronLan2_thayHoanToanKetQuaCu_khongCongDon() {
        EndpointResponseDto dto1 = dto("e1", "/v1/foo", GatewayMethod.GET);
        cache.reload(List.of(dto1));
        assertThat(cache.findExact("GET", "/v1/foo")).isEqualTo(dto1);

        // Lan reload thu 2 (vi du sau khi xoa endpoint) - danh sach moi rong.
        cache.reload(List.of());

        assertThat(cache.findExact("GET", "/v1/foo")).isNull();
        assertThat(cache.all()).isEmpty();
    }

    private static EndpointResponseDto dto(String id, String path, GatewayMethod method) {
        return new EndpointResponseDto(id, "name", null, path, method, false, "json", List.of(), List.of(), null, null, false, 86400, false, false, 300);
    }
}
