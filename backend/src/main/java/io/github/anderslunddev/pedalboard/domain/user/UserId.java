package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "UserId value must not be null");
	}
}
