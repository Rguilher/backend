package br.com.studiogui.backend.controller;

import br.com.studiogui.backend.config.JWTUserData;
import br.com.studiogui.backend.controller.dto.request.CreateAppointmentRequest;
import br.com.studiogui.backend.controller.dto.response.AppointmentDetailResponse;
import br.com.studiogui.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AppointmentDetailResponse> schedule(@RequestBody @Valid CreateAppointmentRequest request,
            @AuthenticationPrincipal JWTUserData user) {

        AppointmentDetailResponse response = service.schedule(request, user.userId());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
            @AuthenticationPrincipal JWTUserData user) {

        service.cancelAppointment(id, user.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/today")
    public ResponseEntity<List<AppointmentDetailResponse>> getMyAgendaToday(@AuthenticationPrincipal JWTUserData user) {
        List<AppointmentDetailResponse> list = service.listToday(user.userId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/week")
    public ResponseEntity<List<AppointmentDetailResponse>> getMyAgendaWeek(
            @AuthenticationPrincipal JWTUserData user) {
        List<AppointmentDetailResponse> list = service.listUpcomingWeek(user.userId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/month")
    public ResponseEntity<List<AppointmentDetailResponse>> getMyAgendaMonth(
            @AuthenticationPrincipal JWTUserData user) {
        List<AppointmentDetailResponse> list = service.listCurrentMonth(user.userId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<LocalTime>> getAvailability(
            @RequestParam Long professionalId,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LocalTime> slots = service.getAvailability(professionalId, serviceId, date);
        return ResponseEntity.ok(slots);
    }
}
