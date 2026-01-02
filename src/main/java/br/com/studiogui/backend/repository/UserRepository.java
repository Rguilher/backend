package br.com.studiogui.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.studiogui.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
