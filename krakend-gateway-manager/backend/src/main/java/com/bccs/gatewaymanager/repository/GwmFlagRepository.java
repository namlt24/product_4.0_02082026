package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.GwmFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GwmFlagRepository extends JpaRepository<GwmFlag, String> {

    /** Danh sach cờ cua 1 doi - API trang Cờ + export config cho DP sync. */
    List<GwmFlag> findByTeamCode(String teamCode);

    /** Check duplicate flag_code trong team truoc khi tao/sua (unique index V5 chan o DB, day de bao loi ro rang). */
    boolean existsByTeamCodeAndFlagCodeIgnoreCase(String teamCode, String flagCode);

    /** Flag code - DUY NHAT trong team (UK V5). */
    Optional<GwmFlag> findByTeamCodeAndFlagCodeIgnoreCase(String teamCode, String flagCode);

    /**
     * So endpoint cua doi co flagCode xuat hien trong cot flag_rules (validate
     * truoc khi xoa) - tim 'code' co the khop nhieu hon thuc te (vi du code
     * "VIP" khop "VIP_TRIAL") - chi dung de CHAN xoa an toan.
     *
     * KHONG the de JPQL LIKE tren cot CLOB: Oracle tu choi (ORA-00932) khi
     * Hibernate validate query luc khoi dong => CP crashloop (su co image 31).
     * Do doc day trong Java qua danh sach endpoint cua doi (so luong nho).
     */

    /**
     * Cap fingerprint (COUNT + MAX updated_at) cho ETag/304 sync DP - mirror
     * fingerprintByTeamCode cua Upstream/Endpoint/Version (cai bai Object[] get(0)
     * 2026-09-13: aggregate LUON tra 1 row, khai bao List<Object[]> roi get(0)).
     */
    @Query("SELECT COUNT(f), MAX(f.updatedAt) FROM GwmFlag f WHERE f.teamCode = :teamCode")
    List<Object[]> fingerprintByTeamCode(@Param("teamCode") String teamCode);
}
