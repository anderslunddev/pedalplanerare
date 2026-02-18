package io.github.anderslunddev.pedalboard.domain.user;

import java.util.UUID;

/**
 * Object Mother for {@link User} to simplify record construction in tests.
 */
public final class UserMother {

	private UserMother() {
	}

	public static User simple() {
		return new User(UUID.randomUUID(), "testuser", "test@example.com", "hashedpassword");
	}

	public static User withUsername(String username) {
		User base = simple();
		return new User(base.id(), username, base.email(), base.password());
	}

	public static User withEmail(String email) {
		User base = simple();
		return new User(base.id(), base.username(), email, base.password());
	}

	public static User withId(UUID id) {
		User base = simple();
		return new User(id, base.username(), base.email(), base.password());
	}
}
