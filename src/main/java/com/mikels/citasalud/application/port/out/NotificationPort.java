package com.mikels.citasalud.application.port.out;

import com.mikels.citasalud.domain.model.Cita;

public interface NotificationPort {

    NotificationResult enviarConfirmacion(Cita cita, String numeroWhatsApp);

    record NotificationResult(boolean exitoso) {

        public static NotificationResult exitosa() {
            return new NotificationResult(true);
        }

        public static NotificationResult fallida() {
            return new NotificationResult(false);
        }
    }
}
