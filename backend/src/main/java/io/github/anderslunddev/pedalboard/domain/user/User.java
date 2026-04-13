package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Simple domain user. Password is expected to be stored in hashed form (see
 * {@code UserService} when creating or updating passwords).
 */
public record User(UUID id, String username, Email email, String password, Role role) {

	public User {
		Objects.requireNonNull(id, "User id must not be null");
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		Objects.requireNonNull(email, "Email must not be null");
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password must not be blank");
		}
		if (role == null) {
			role = Role.USER;
		}
	}
}
