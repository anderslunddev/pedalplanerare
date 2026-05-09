package io.github.anderslunddev.pedalboard.domain.board;

import java.util.Objects;


public record BoardName(String value) {

	public static final int MAX_LENGTH = 100;

	public BoardName(String value) {
		this.value = Objects.requireNonNull(value, "Board name must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Board name must not be blank");
		}
		if (value.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Board name must be at most " + MAX_LENGTH + " characters");
		}
	}

	@Override
	public String toString() {
		return "BoardName{value='" + value + "'}";
	}
}
