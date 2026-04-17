package br.com.studiogui.backend.repository;

import java.util.List;
import java.util.Optional;

import br.com.studiogui.backend.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.studiogui.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findUserByEmail(String username);
    boolean existsByEmail(String email);
    Page<User> findByRole(UserRole role, Pageable pageable);
}
