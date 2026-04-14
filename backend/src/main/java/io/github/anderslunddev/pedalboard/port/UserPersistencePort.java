package io.github.anderslunddev.pedalboard.port;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outgoing port: load and persist users (credentials and roles).
 */
public interface UserPersistencePort {

	User createUser(UserName userName, Email email, String password, Role role);

	Optional<User> findByUsername(UserName userName);

	Optional<User> findById(UUID id);

	List<User> findAll();

	boolean existsByUsername(UserName userName);

	boolean existsByEmail(Email email);

	User updateRole(UUID userId, Role role);

	User updatePassword(UUID userId, String hashedPassword);

	void deleteById(UUID userId);
}
