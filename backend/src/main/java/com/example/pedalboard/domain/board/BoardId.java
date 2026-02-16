package com.example.pedalboard.domain.board;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for boards.
 */
public record BoardId(UUID value) {

	public BoardId {
		Objects.requireNonNull(value, "BoardId value must not be null");
	}
}
