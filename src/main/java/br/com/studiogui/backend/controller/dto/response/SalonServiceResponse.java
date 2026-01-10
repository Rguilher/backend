package br.com.studiogui.backend.controller.dto.response;

import br.com.studiogui.backend.model.SalonService;

import java.math.BigDecimal;

public record SalonServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMin
) {
    public SalonServiceResponse(SalonService entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDurationMin()
        );
    }
}
