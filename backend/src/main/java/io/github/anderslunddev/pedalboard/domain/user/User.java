package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Simple domain user. Password is expected to be stored in hashed form (see
 * {@code UserService.register}).
 */
public record User(UUID id, String username, String email, String password, String role) {

	public static final String ROLE_USER = "USER";
	public static final String ROLE_ADMIN = "ADMIN";

	public User {
		Objects.requireNonNull(id, "User id must not be null");
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email must not be blank");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password must not be blank");
		}
		if (role == null || role.isBlank()) {
			role = ROLE_USER;
		}
	}

	public boolean isAdmin() {
		return ROLE_ADMIN.equals(role);
	}
}
