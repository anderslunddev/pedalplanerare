package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter {

	private final UserRepository userRepository;
	private final UserModelConverter converter;

	public UserRepositoryAdapter(UserRepository userRepository, UserModelConverter converter) {
		this.userRepository = userRepository;
		this.converter = converter;
	}

	public User createUser(String username, String email, String password) {
		UserModel toSave = converter.toEntity(username, email, password);
		UserModel saved = userRepository.save(toSave);
		return converter.toDomain(saved);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username).map(converter::toDomain);
	}

	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
}
