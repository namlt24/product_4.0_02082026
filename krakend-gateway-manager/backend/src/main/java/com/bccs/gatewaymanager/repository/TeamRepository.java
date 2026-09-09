package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {

    /** Dung boi ApiKeyAuthFilter tren MOI request /api/** (tru /api/teams/**) - tra cuu doi theo key client gui len. */
    Optional<Team> findByApiKey(String apiKey);

    List<Team> findAllByOrderByTeamNameAsc();
}
