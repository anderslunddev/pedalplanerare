package com.example.pedalboard.domain.pedal;

import java.util.Objects;

/**
 * Domain value object representing a pedal placement number. Placement must be
 * a positive integer.
 */
public record Placement(int value) {

	public Placement {
		if (value <= 0) {
			throw new IllegalArgumentException("Placement must be greater than zero, got: " + value);
		}
	}

	@Override
	public String toString() {
		return "Placement{value=" + value + "}";
	}
}
