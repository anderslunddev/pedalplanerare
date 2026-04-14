package io.github.anderslunddev.pedalboard.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity data used for JWT issuance and similar auth flows — no credentials.
 */
public record AuthPrincipal(UUID userId, UserName userName, Role role) {

	public AuthPrincipal {
		Objects.requireNonNull(userId, "userId must not be null");
		Objects.requireNonNull(userName, "userName must not be null");
		Objects.requireNonNull(role, "role must not be null");
	}

	public static AuthPrincipal fromUser(User user) {
		Objects.requireNonNull(user, "user must not be null");
		return new AuthPrincipal(user.id(), user.userName(), user.role());
	}
}
