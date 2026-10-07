package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.GwmFlagHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GwmFlagHistoryRepository extends JpaRepository<GwmFlagHistory, String> {

    /** Lich su 1 cờ, moi nhat truoc - trang Cờ (xem lich su 1 cờ). */
    List<GwmFlagHistory> findByFlagIdOrderByChangedAtDesc(String flagId);

    /** Lich su gan nhat cua ca doi - trang Cờ (khoi "Thay doi gan day"). */
    List<GwmFlagHistory> findByTeamCodeOrderByChangedAtDesc(String teamCode);
}
