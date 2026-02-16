package io.github.anderslunddev.pedalboard.domain.board;

import java.util.Objects;

/**
 * Domain value object representing a board name. Board name must not be null or
 * blank.
 */
public record BoardName(String value) {

	public BoardName(String value) {
		this.value = Objects.requireNonNull(value, "Board name must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Board name must not be blank");
		}
	}

	@Override
	public String toString() {
		return "BoardName{value='" + value + "'}";
	}
}
