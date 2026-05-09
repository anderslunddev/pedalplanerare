package io.github.anderslunddev.pedalboard.domain.user;

import java.util.regex.Pattern;

/**
 * Normalized email address (trimmed, lower-cased) with structural validation.
 * Intentionally stricter than "non-blank" and aligned with common mailbox shapes
 * (local part, single {@code @}, domain with a TLD label).
 * <p>
 * Domain VO; we deliberately don't depend on {@code jakarta.validation} here —
 * validation is enforced at construction so invalid emails can never exist in
 * the domain model.
 */
public final class Email implements Comparable<Email> {

	/** RFC 5321 maximum length for the address. */
	public static final int MAX_LENGTH = 254;

	/**
	 * Pragmatic pattern: local part + {@code @} + domain labels ending in a 2+ letter TLD.
	 * Not a full RFC 5322 implementation; rejects obvious garbage and multiple {@code @}.
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$");

	private final String value;

	private Email(String value) {
		this.value = value;
	}

	/**
	 * Parses and normalizes user-supplied input into an {@link Email}.
	 *
	 * @throws IllegalArgumentException if null, blank, too long, or not a plausible address
	 */
	public static Email parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("Email must not be blank");
		}
		String normalized = raw.strip().toLowerCase();
		if (normalized.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Email is too long");
		}
		if (normalized.indexOf('@') != normalized.lastIndexOf('@')) {
			throw new IllegalArgumentException("Email must be a valid email address");
		}
		if (!PATTERN.matcher(normalized).matches()) {
			throw new IllegalArgumentException("Email must be a valid email address");
		}
		return new Email(normalized);
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
		Email email = (Email) o;
		return value.equals(email.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int compareTo(Email o) {
		return value.compareTo(o.value);
	}
}
