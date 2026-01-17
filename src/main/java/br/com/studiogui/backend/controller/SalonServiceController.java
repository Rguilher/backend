package br.com.studiogui.backend.controller;

import br.com.studiogui.backend.controller.dto.request.CreateServiceRequest;
import br.com.studiogui.backend.controller.dto.response.SalonServiceResponse;
import br.com.studiogui.backend.model.SalonService;
import br.com.studiogui.backend.service.SalonServiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/services")
public class SalonServiceController {

    private final SalonServiceService service;

    public SalonServiceController(SalonServiceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalonServiceResponse> create(@RequestBody @Valid CreateServiceRequest data,
            UriComponentsBuilder uriBuilder) {

        SalonService newService = service.createService(data);
        URI uri = uriBuilder.path("/services/{id}").buildAndExpand(newService.getId()).toUri();
        return ResponseEntity.created(uri).body(new SalonServiceResponse(newService));
    }

    @GetMapping
    public ResponseEntity<List<SalonServiceResponse>> listAll() {

        List<SalonService> services = service.getAllServices();
        List<SalonServiceResponse> response = services.stream()
                .map(SalonServiceResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
