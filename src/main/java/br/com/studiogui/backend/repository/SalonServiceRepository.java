package br.com.studiogui.backend.repository;

import br.com.studiogui.backend.model.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
    List<SalonService> findAllByActiveTrue();
    boolean existsByNameAndActiveTrue(String name);
}
