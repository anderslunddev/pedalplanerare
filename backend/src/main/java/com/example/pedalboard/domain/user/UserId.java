package com.example.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for users to avoid mixing UUIDs between aggregates.
 */
public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "UserId value must not be null");
	}
}
