package io.github.anderslunddev.pedalboard;

import io.github.anderslunddev.pedalboard.model.UserRepositoryAdapter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner seedDefaultUsers(UserRepositoryAdapter userRepositoryAdapter, PasswordEncoder passwordEncoder) {
		return args -> {
			String username1 = "anders";
			if (!userRepositoryAdapter.existsByUsername(username1)) {
				String encoded1 = passwordEncoder.encode("pass");
				userRepositoryAdapter.createUser(username1, "anders@example.com", encoded1);
			}

			String username2 = "panders";
			if (!userRepositoryAdapter.existsByUsername(username2)) {
				String encoded2 = passwordEncoder.encode("word");
				userRepositoryAdapter.createUser(username2, "panders@example.com", encoded2);
			}
		};
	}
}
