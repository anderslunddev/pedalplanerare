package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Converter between {@link UserModel} and domain {@link User}.
 */
@Component
class UserModelConverter {

	User toDomain(UserModel entity) {
		Objects.requireNonNull(entity, "UserModel must not be null");
		return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPassword());
	}

	UserModel toEntity(String username, String email, String password) {
		Objects.requireNonNull(username, "username must not be null");
		Objects.requireNonNull(email, "email must not be null");
		Objects.requireNonNull(password, "password must not be null");
		return new UserModel(username, email, password);
	}
}

