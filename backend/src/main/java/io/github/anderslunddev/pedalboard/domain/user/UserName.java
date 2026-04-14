package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;

/**
 * Normalized login username (trimmed, non-blank) stored in persistence.
 */
public record UserName(String value) {

	public static final int MAX_LENGTH = 255;

	public UserName {
		Objects.requireNonNull(value, "Username must not be null");
		value = value.strip();
		if (value.isBlank()) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		if (value.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Username is too long");
		}
	}

	/**
	 * Parses user-supplied input into a {@link UserName}.
	 *
	 * @throws IllegalArgumentException if null, blank after trim, or too long
	 */
	public static UserName parse(String raw) {
		if (raw == null) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		return new UserName(raw);
	}

	@Override
	public String toString() {
		return "UserName{value='" + value + "'}";
	}
}
