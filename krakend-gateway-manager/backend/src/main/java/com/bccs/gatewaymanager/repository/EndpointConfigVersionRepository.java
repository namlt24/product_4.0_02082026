package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.EndpointConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EndpointConfigVersionRepository extends JpaRepository<EndpointConfigVersion, String> {

    List<EndpointConfigVersion> findByEndpointIdOrderByVersionNumberDesc(String endpointId);

    Optional<EndpointConfigVersion> findTopByEndpointIdOrderByVersionNumberDesc(String endpointId);

    /** Tra ve so ban ghi da xoa - goi trong EndpointService.delete() de don version cua 1 endpoint bi xoa. */
    long deleteByEndpointId(String endpointId);
}
