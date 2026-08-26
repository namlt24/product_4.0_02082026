package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.UpstreamService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpstreamServiceRepository extends JpaRepository<UpstreamService, String> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, String id);

    Optional<UpstreamService> findByName(String name);

    List<UpstreamService> findAllByOrderByNameAsc();
}
