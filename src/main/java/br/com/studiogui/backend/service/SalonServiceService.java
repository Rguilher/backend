package br.com.studiogui.backend.service;

import br.com.studiogui.backend.controller.dto.request.CreateServiceRequest;
import br.com.studiogui.backend.model.SalonService;
import br.com.studiogui.backend.repository.SalonServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalonServiceService {

    private final SalonServiceRepository repository;

    public SalonServiceService(SalonServiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SalonService createService(CreateServiceRequest request) {

        if (repository.existsByNameAndActiveTrue(request.name())) {
            throw new IllegalArgumentException("Já existe um serviço ativo com esse nome.");
        }
        SalonService newService = new SalonService();
        newService.setName(request.name());
        newService.setDescription(request.description());
        newService.setPrice(request.price());
        newService.setDurationMin(45);
        newService.setActive(true);

       return repository.save(newService);
    }


    public List<SalonService> getAllServices() {
        return repository.findAllByActiveTrue();
    }

    @Transactional
    public void deleteService(Long id) {
        SalonService service = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));

        service.setActive(false);
        repository.save(service);
    }
}
