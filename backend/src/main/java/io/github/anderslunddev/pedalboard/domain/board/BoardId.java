package io.github.anderslunddev.pedalboard.domain.board;

import java.util.Objects;
import java.util.UUID;

public record BoardId(UUID value) {

	public BoardId {
		Objects.requireNonNull(value, "BoardId value must not be null");
	}
}
