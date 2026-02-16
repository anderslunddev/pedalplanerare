package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter {

	private final UserRepository userRepository;

	public UserRepositoryAdapter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User createUser(String username, String email, String password) {
		UserModel saved = userRepository.save(new UserModel(username, email, password));
		return toDomain(saved);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username).map(UserRepositoryAdapter::toDomain);
	}

	public boolean existsByUsername(String username) {
		return userRepository.findByUsername(username).isPresent();
	}

	public boolean existsByEmail(String email) {
		return userRepository.findByEmail(email).isPresent();
	}

	private static User toDomain(UserModel entity) {
		if (entity == null)
			return null;
		return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPassword());
	}
}
