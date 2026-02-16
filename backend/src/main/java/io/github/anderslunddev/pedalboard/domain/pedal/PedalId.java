package io.github.anderslunddev.pedalboard.domain.pedal;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for pedals.
 */
public record PedalId(UUID value) {

	public PedalId {
		Objects.requireNonNull(value, "PedalId value must not be null");
	}
}
