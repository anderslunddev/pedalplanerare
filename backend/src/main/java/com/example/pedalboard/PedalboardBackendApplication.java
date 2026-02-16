package com.example.pedalboard;

import com.example.pedalboard.model.UserRepositoryAdapter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
	}

	/**
	 * Seed default users for quick manual testing. Users: anders / pass, panders /
	 * word
	 */
	@Bean
	CommandLineRunner seedDefaultUsers(UserRepositoryAdapter userRepositoryAdapter, PasswordEncoder passwordEncoder) {
		return args -> {
			// Seed user: anders
			String username1 = "anders";
			if (!userRepositoryAdapter.existsByUsername(username1)) {
				String encoded1 = passwordEncoder.encode("pass");
				userRepositoryAdapter.createUser(username1, "anders@example.com", encoded1);
			}

			// Seed user: panders
			String username2 = "panders";
			if (!userRepositoryAdapter.existsByUsername(username2)) {
				String encoded2 = passwordEncoder.encode("word");
				userRepositoryAdapter.createUser(username2, "panders@example.com", encoded2);
			}
		};
	}
}
