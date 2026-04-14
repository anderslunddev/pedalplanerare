package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Simple domain user. Password is expected to be stored in hashed form (see
 * {@code UserService} when creating or updating passwords).
 */
public record User(UUID id, UserName userName, Email email, String password, Role role) {

	public User {
		Objects.requireNonNull(id, "User id must not be null");
		Objects.requireNonNull(userName, "Username must not be null");
		Objects.requireNonNull(email, "Email must not be null");
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password must not be blank");
		}
		if (role == null) {
			role = Role.USER;
		}
	}
}
