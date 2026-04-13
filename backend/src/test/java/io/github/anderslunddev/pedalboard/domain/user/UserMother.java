package io.github.anderslunddev.pedalboard.domain.user;

import java.util.UUID;

/**
 * Object Mother for {@link User} to simplify record construction in tests.
 */
public final class UserMother {

	private UserMother() {
	}

	public static User simple() {
		return new User(UUID.randomUUID(), "testuser", Email.parse("test@example.com"), "hashedpassword", Role.USER);
	}

	public static User admin() {
		return new User(UUID.randomUUID(), "admin", Email.parse("admin@example.com"), "hashedpassword", Role.ADMIN);
	}

	public static User withUsername(String username) {
		User base = simple();
		return new User(base.id(), username, base.email(), base.password(), base.role());
	}

	public static User withEmail(String email) {
		User base = simple();
		return new User(base.id(), base.username(), Email.parse(email), base.password(), base.role());
	}

	public static User withId(UUID id) {
		User base = simple();
		return new User(id, base.username(), base.email(), base.password(), base.role());
	}
}
