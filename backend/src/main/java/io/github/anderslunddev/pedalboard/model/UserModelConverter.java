package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserName;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Converter between {@link UserModel} and domain {@link User}.
 */
@Component
class UserModelConverter {

	User toDomain(UserModel entity) {
		Objects.requireNonNull(entity, "UserModel must not be null");
		return new User(entity.getId(), UserName.parse(entity.getUsername()), Email.parse(entity.getEmail()),
				entity.getPassword(), entity.getRole());
	}

	UserModel toEntity(UserName userName, Email email, String password, Role role) {
		Objects.requireNonNull(userName, "userName must not be null");
		Objects.requireNonNull(email, "email must not be null");
		Objects.requireNonNull(password, "password must not be null");
		return new UserModel(userName.value(), email.value(), password, role);
	}
}
