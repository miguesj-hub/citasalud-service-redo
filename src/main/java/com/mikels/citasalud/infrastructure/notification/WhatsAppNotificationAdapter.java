package com.mikels.citasalud.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mikels.citasalud.application.port.out.NotificationPort;
import com.mikels.citasalud.domain.model.Cita;

/**
 * Implementacion de referencia contra la WhatsApp Business Cloud API (ver research.md §2).
 * El endpoint/credenciales concretos se externalizan como configuracion; un fallo de red o del
 * proveedor se traduce en un resultado fallido sin lanzar excepcion, para no bloquear el registro
 * de la cita (FR-003/FR-010).
 */
@Component
public class WhatsAppNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationAdapter.class);
    private static final String ENDPOINT = "https://graph.facebook.com/v20.0/me/messages";

    private final RestClient restClient = RestClient.create();

    @Override
    public NotificationResult enviarConfirmacion(Cita cita, String numeroWhatsApp) {
        try {
            restClient.post()
                    .uri(ENDPOINT)
                    .body(new MensajeConfirmacion(numeroWhatsApp, mensajeConfirmacion(cita)))
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.exitosa();
        } catch (RestClientException ex) {
            log.warn("No se pudo enviar la confirmacion por WhatsApp para la cita {}: {}",
                    cita.getId(), ex.getMessage());
            return NotificationResult.fallida();
        }
    }

    private String mensajeConfirmacion(Cita cita) {
        return "Tu cita ha sido confirmada para el %s a las %s".formatted(cita.getFecha(), cita.getHoraInicio());
    }

    private record MensajeConfirmacion(String destinatario, String mensaje) {
    }
}
