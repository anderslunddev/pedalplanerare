package io.github.anderslunddev.pedalboard.domain.pedal;

import java.util.Objects;


public record PedalName(String value) {

	public static final int MAX_LENGTH = 100;

	public PedalName {
		Objects.requireNonNull(value, "Pedal name must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Pedal name must not be blank");
		}
		if (value.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Pedal name must be at most " + MAX_LENGTH + " characters");
		}
	}
}
