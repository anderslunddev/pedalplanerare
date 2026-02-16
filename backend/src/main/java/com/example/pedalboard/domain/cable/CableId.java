package com.example.pedalboard.domain.cable;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for cables.
 */
public record CableId(UUID value) {

	public CableId {
		Objects.requireNonNull(value, "CableId value must not be null");
	}
}
