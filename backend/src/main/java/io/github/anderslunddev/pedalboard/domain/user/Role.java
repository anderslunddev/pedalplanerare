package io.github.anderslunddev.pedalboard.domain.user;

/**
 * Application role for authorization. Persisted as the enum name (e.g. {@code USER}, {@code ADMIN}).
 */
public enum Role {
	USER,
	ADMIN;

	/**
	 * Parses API / wire / database values (case-insensitive). Blank or null defaults to {@link #USER}.
	 *
	 * @throws IllegalArgumentException if the value is not a known role name
	 */
	public static Role parse(String value) {
		if (value == null || value.isBlank()) {
			return USER;
		}
		try {
			return Role.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid role: " + value);
		}
	}
}
