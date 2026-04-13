package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.port.UserPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserPersistencePort {

	private final UserRepository userRepository;
	private final UserModelConverter converter;

	public UserRepositoryAdapter(UserRepository userRepository, UserModelConverter converter) {
		this.userRepository = userRepository;
		this.converter = converter;
	}

	public User createUser(String username, Email email, String password, Role role) {
		UserModel toSave = converter.toEntity(username, email, password, role);
		UserModel saved = userRepository.save(toSave);
		return converter.toDomain(saved);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username).map(converter::toDomain);
	}

	public Optional<User> findById(UUID id) {
		return userRepository.findById(id).map(converter::toDomain);
	}

	public List<User> findAll() {
		return userRepository.findAll().stream().map(converter::toDomain).toList();
	}

	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean existsByEmail(Email email) {
		return userRepository.existsByEmail(email.value());
	}

	public User updateRole(UUID userId, Role role) {
		UserModel model = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		model.setRole(role);
		return converter.toDomain(userRepository.save(model));
	}

	public User updatePassword(UUID userId, String hashedPassword) {
		UserModel model = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		model.setPassword(hashedPassword);
		return converter.toDomain(userRepository.save(model));
	}

	public void deleteById(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new IllegalArgumentException("User not found");
		}
		userRepository.deleteById(userId);
	}
}
