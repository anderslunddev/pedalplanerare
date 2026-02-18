package io.github.anderslunddev.pedalboard;

import io.github.anderslunddev.pedalboard.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
	}

	@Bean
	@Profile("!prod")
	CommandLineRunner seedDefaultUsers(UserService userService) {
		return args -> {
			tryRegister(userService, "anders", "anders@example.com", "pass");
			tryRegister(userService, "panders", "panders@example.com", "word");
		};
	}

	private static void tryRegister(UserService userService, String username, String email, String password) {
		try {
			userService.register(username, email, password);
		} catch (IllegalArgumentException e) {
			// already exists
		}
	}
}
