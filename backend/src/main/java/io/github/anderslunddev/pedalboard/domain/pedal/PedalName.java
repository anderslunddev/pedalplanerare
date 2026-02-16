package io.github.anderslunddev.pedalboard.domain.pedal;

import java.util.Objects;

/**
 * Strongly-typed pedal name to avoid mixing raw strings and to centralize
 * validation.
 */
public record PedalName(String value) {

	public PedalName {
		Objects.requireNonNull(value, "Pedal name must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("Pedal name must not be blank");
		}
	}
}
