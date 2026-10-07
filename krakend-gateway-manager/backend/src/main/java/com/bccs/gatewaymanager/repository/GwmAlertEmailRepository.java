package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.GwmAlertEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GwmAlertEmailRepository extends JpaRepository<GwmAlertEmail, String> {

    /** Danh sach nguoi nhan cua 1 doi - dung o CA 2 noi: API trang nguoi nhan (list) va AlertEmailService khi gui mail (resolveRecipients). */
    List<GwmAlertEmail> findByTeamCode(String teamCode);

    /** Check duplicate truoc khi them - unique index V4 cung chan o DB, day de bao loi ro rang (GW-ALERT-EMAIL-DUPLICATE). */
    boolean existsByTeamCodeAndEmailIgnoreCase(String teamCode, String email);
}
