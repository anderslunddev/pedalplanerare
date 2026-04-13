package io.github.anderslunddev.pedalboard;

import io.github.anderslunddev.pedalboard.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	private static final Logger log = LoggerFactory.getLogger(PedalboardBackendApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
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
}
