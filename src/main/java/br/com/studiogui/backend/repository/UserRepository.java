package br.com.studiogui.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.studiogui.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findUserByEmail(String username);
}
