package br.com.studiogui.backend;

import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.model.UserRole;
import br.com.studiogui.backend.repository.UserRepository;
import br.com.studiogui.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encoder;
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		var userAdmin = userRepository.findUserByEmail("admin@admin.com");
		if (userAdmin.isEmpty()){
			User user = User.builder()
					.name("admin")
					.email("admin@admin.com")
					.password(encoder.encode("admin"))
					.role(UserRole.ADMIN)
					.phone("0000-0000")
					.build();
			userRepository.save(user);
			System.out.println("Initial ADM created.");
		}
	}
}
