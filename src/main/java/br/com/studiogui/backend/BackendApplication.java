package br.com.studiogui.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(
			@Value("${spring.mail.username:NOT_FOUND}") String mailUser,
			@Value("${spring.mail.password:NOT_FOUND}") String mailPass) {
		return args -> {
			System.out.println("========== EMAIL CONFIGURATION CHECK ==========");
			System.out.println("MAIL_USERNAME: " + mailUser);

			if (mailPass.equals("NOT_FOUND")) {
				System.err.println("MAIL_PASSWORD: NOT CONFIGURED! ❌");
			} else {
				System.out.println("MAIL_PASSWORD: CONFIGURED (HIDDEN) ✅");
			}
			System.out.println("===============================================");
		};
	}
}
