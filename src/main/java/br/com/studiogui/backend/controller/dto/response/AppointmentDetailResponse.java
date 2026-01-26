package br.com.studiogui.backend.controller.dto.response;

import br.com.studiogui.backend.model.Appointment;
import br.com.studiogui.backend.model.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentDetailResponse(
        Long id,
        String clientName,
        String professionalName,
        String serviceName,
        BigDecimal price,

        LocalDateTime startTime,
        LocalDateTime endTime, // Calcularemos isso no Java

        AppointmentStatus status
) {
    public AppointmentDetailResponse(Appointment entity) {
        this(
                entity.getId(),
                entity.getClient().getName(),
                entity.getProfessional().getName(),
                entity.getService().getName(),
                entity.getService().getPrice(),
                entity.getDateTime(),
                entity.getDateTime().plusMinutes(entity.getService().getDurationMin()),
                entity.getStatus()
        );
    }
}
