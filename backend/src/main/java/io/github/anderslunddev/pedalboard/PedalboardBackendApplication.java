package io.github.anderslunddev.pedalboard;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	private static final Logger log = LoggerFactory.getLogger(PedalboardBackendApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
	}

	@Bean
	@Profile("!prod")
	CommandLineRunner seedDefaultUsers(UserService userService) {
		return args -> {
			tryRegister(userService, "anders", "anders@example.com", "pass", User.ROLE_ADMIN);
			tryRegister(userService, "panders", "panders@example.com", "word", User.ROLE_USER);
		};
	}

	/**
	 * In any environment, if ADMIN_USERNAME is set, promote that user to ADMIN on
	 * startup. Useful for bootstrapping the first admin in production.
	 */
	@Bean
	CommandLineRunner bootstrapAdmin(UserService userService,
			@Value("${app.admin.bootstrap-username:}") String adminUsername) {
		return args -> {
			if (adminUsername == null || adminUsername.isBlank()) {
				return;
			}
			try {
				userService.promoteToAdmin(adminUsername);
				log.info("Promoted user '{}' to ADMIN", adminUsername);
			} catch (IllegalArgumentException e) {
				log.warn("Could not promote '{}' to admin: {}", adminUsername, e.getMessage());
			}
		};
	}

	private static void tryRegister(UserService userService, String username, String email, String password,
			String role) {
		try {
			userService.register(username, email, password, role);
		} catch (IllegalArgumentException e) {
			// already exists
		}
	}
}
