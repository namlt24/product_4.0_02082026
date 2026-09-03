package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Router 2 tang moi them (finding #1 tu senior review): da so endpoint la path
 * "tinh" (khong {param}) nen tra O(1) qua findExact()/exactIndex, chi endpoint
 * co {param} moi roi vao patternEndpoints() (danh sach nho hon "all()", con lai
 * cho DynamicDispatcherController tu quet PathPattern.matches()).
 */
@ExtendWith(MockitoExtension.class)
class EndpointRegistryCacheTest {

    @Mock
    private EndpointConfigRepository repository;
    @Mock
    private EndpointMapper mapper;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private TransactionStatus transactionStatus;

    private EndpointRegistryCache cache;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        cache = new EndpointRegistryCache(repository, mapper, transactionManager);
    }

    @Test
    void reload_pathTinh_vaoExactIndex_traO1() {
        EndpointConfig entity = EndpointConfig.builder().id("e1").name("n").path("/v1/foo").method(GatewayMethod.GET).build();
        EndpointResponseDto dto = dto("e1", "/v1/foo", GatewayMethod.GET);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(dto);

        cache.reload();

        assertThat(cache.findExact("GET", "/v1/foo")).isEqualTo(dto);
        assertThat(cache.patternEndpoints()).isEmpty();
        assertThat(cache.all()).containsExactly(dto);
    }

    @Test
    void reload_pathCoParam_vaoPatternEndpoints_khongVaoExactIndex() {
        EndpointConfig entity = EndpointConfig.builder().id("e2").name("n").path("/v1/staff/{staffId}").method(GatewayMethod.GET).build();
        EndpointResponseDto dto = dto("e2", "/v1/staff/{staffId}", GatewayMethod.GET);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(dto);

        cache.reload();

        // exactIndex la tra cuu CHUOI-DUNG-CHUOI, khong tu suy pattern - endpoint
        // co {param} PHAI di qua patternEndpoints()/PathPattern o dispatcher.
        assertThat(cache.findExact("GET", "/v1/staff/{staffId}")).isNull();
        assertThat(cache.findExact("GET", "/v1/staff/123")).isNull();
        assertThat(cache.patternEndpoints()).containsExactly(dto);
    }

    @Test
    void findExact_saiMethod_traNull() {
        EndpointConfig entity = EndpointConfig.builder().id("e1").name("n").path("/v1/foo").method(GatewayMethod.GET).build();
        EndpointResponseDto dto = dto("e1", "/v1/foo", GatewayMethod.GET);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(dto);

        cache.reload();

        assertThat(cache.findExact("POST", "/v1/foo")).isNull();
    }

    @Test
    void reload_tronLan2_thayHoanToanKetQuaCu_khongCongDon() {
        EndpointConfig e1 = EndpointConfig.builder().id("e1").name("n1").path("/v1/foo").method(GatewayMethod.GET).build();
        EndpointResponseDto dto1 = dto("e1", "/v1/foo", GatewayMethod.GET);
        when(repository.findAll()).thenReturn(List.of(e1));
        when(mapper.toResponseDto(e1)).thenReturn(dto1);
        cache.reload();
        assertThat(cache.findExact("GET", "/v1/foo")).isEqualTo(dto1);

        // Lan reload thu 2 (vi du sau khi xoa endpoint) - repository gio rong.
        when(repository.findAll()).thenReturn(List.of());
        cache.reload();

        assertThat(cache.findExact("GET", "/v1/foo")).isNull();
        assertThat(cache.all()).isEmpty();
    }

    private static EndpointResponseDto dto(String id, String path, GatewayMethod method) {
        return new EndpointResponseDto(id, "name", null, path, method, false, "json", List.of(), List.of(), null, null, false, 86400, false, false, 300);
    }
}
