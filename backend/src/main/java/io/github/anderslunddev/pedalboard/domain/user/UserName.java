package io.github.anderslunddev.pedalboard.domain.user;

/**
 * Normalized login username (trimmed, non-blank) stored in persistence.
 * Construct only via {@link #parse(String)} so string input always goes through validation.
 */
public final class UserName {

	public static final int MAX_LENGTH = 255;

	private final String value;

	private UserName(String raw) {
		if (raw == null) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		String normalized = raw.strip();
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Username must not be blank");
		}
		if (normalized.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Username is too long");
		}
		this.value = normalized;
	}

	/**
	 * Parses user-supplied input into a {@link UserName}.
	 *
	 * @throws IllegalArgumentException if null, blank after trim, or too long
	 */
	public static UserName parse(String raw) {
		return new UserName(raw);
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		UserName userName = (UserName) o;
		return value.equals(userName.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return "UserName{value='" + value + "'}";
	}
}
