package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.UpstreamService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpstreamServiceRepository extends JpaRepository<UpstreamService, String> {

    // --- Method CU (khong con team_code) - giu lai cho EndpointMapper.findUpstreamOrThrow()
    // tra bang ID (ID da la khoa duy nhat toan cuc du sau nay them team_code, chi
    // CHUA tu no khang dinh Upstream do co thuoc CurrentTeamContext hay khong - xem
    // EndpointMapper, noi tu kiem tra rieng .getTeamCode() sau khi tra ID). ---
    Optional<UpstreamService> findByName(String name);

    // --- Method MOI, scoped theo team_code - dung cho toan bo Control Plane CRUD. ---
    boolean existsByTeamCodeAndName(String teamCode, String name);

    boolean existsByTeamCodeAndNameAndIdNot(String teamCode, String name, String id);

    Optional<UpstreamService> findByIdAndTeamCode(String id, String teamCode);

    List<UpstreamService> findAllByTeamCodeOrderByNameAsc(String teamCode);
}
