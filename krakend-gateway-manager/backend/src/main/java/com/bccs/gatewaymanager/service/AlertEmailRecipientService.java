package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.entity.GwmAlertEmail;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.GwmAlertEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Control-Plane-only - quan ly nguoi nhan EMAIL canh bao cua doi (bang
 * GWM_ALERT_EMAIL - DDL V4). Doi tu quan ly bang team key qua API
 * /api/alerts/recipients; AlertEmailService dung findByTeamCode khi resolve
 * nguoi nhan truoc khi gui mail.
 *
 * Multi-tenant: team code LUON lay tu CurrentTeamContext (ApiKeyAuthFilter da
 * xac thuc) - KHONG nhan tu path/body, nen doi khong the thay/xoa dia chi cua
 * doi khac ngay ca khi biet id.
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class AlertEmailRecipientService {

    /** Chan 1 doi khai qua dang dia chi - spam mail rat kho xu ly sach sau nay. */
    static final int MAX_RECIPIENTS_PER_TEAM = 10;

    private final GwmAlertEmailRepository repository;

    /** Danh sach nguoi nhan cua doi hien tai (moi nhat truoc theo thu tu tao). */
    public List<GwmAlertEmail> list() {
        String teamCode = CurrentTeamContext.require();
        return repository.findByTeamCode(teamCode);
    }

    public GwmAlertEmail add(String email) {
        String teamCode = CurrentTeamContext.require();
        String normalized = email == null ? "" : email.trim();
        if (!AlertEmailService.isValidEmail(normalized)) {
            throw new BusinessException("GW-ALERT-EMAIL-INVALID",
                    "Dia chi email khong dung dinh dang: '" + normalized + "'.");
        }
        if (repository.existsByTeamCodeAndEmailIgnoreCase(teamCode, normalized)) {
            throw new BusinessException("GW-ALERT-EMAIL-DUPLICATE",
                    "Dia chi '" + normalized + "' da co trong danh sach nguoi nhan cua doi " + teamCode + ".");
        }
        if (repository.findByTeamCode(teamCode).size() >= MAX_RECIPIENTS_PER_TEAM) {
            throw new BusinessException("GW-ALERT-EMAIL-MAX",
                    "Doi " + teamCode + " da khai toi da " + MAX_RECIPIENTS_PER_TEAM + " nguoi nhan email canh bao.");
        }
        GwmAlertEmail saved = repository.save(GwmAlertEmail.builder()
                .teamCode(teamCode)
                .email(normalized)
                .build());
        log.info("Doi {} them nguoi nhan email canh bao: {}", teamCode, normalized);
        return saved;
    }

    public void delete(String id) {
        String teamCode = CurrentTeamContext.require();
        GwmAlertEmail row = repository.findById(id)
                .filter(r -> r.getTeamCode().equals(teamCode)) // KHONG cho doi xoa cua doi khac
                .orElseThrow(() -> new BusinessException("GW-ALERT-EMAIL-NOT-FOUND",
                        "Khong tim thay nguoi nhan email id=" + id + " thuoc doi " + teamCode + "."));
        repository.delete(row);
        log.info("Doi {} xoa nguoi nhan email canh bao: {}", teamCode, row.getEmail());
    }
}
