package io.github.anderslunddev.pedalboard.service.user;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.model.UserRepositoryAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

	private final UserRepositoryAdapter userRepositoryAdapter;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepositoryAdapter userRepositoryAdapter, PasswordEncoder passwordEncoder) {
		this.userRepositoryAdapter = userRepositoryAdapter;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User register(String username, String email, String password) {
		if (userRepositoryAdapter.existsByUsername(username)) {
			throw new IllegalArgumentException("Username is already taken");
		}
		if (userRepositoryAdapter.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}
		String hashed = passwordEncoder.encode(password);
		return userRepositoryAdapter.createUser(username, email, hashed);
	}

	// login is now handled via AuthenticationManager/Jwt in the controller
	public Optional<User> findByUsername(String username) {
		return userRepositoryAdapter.findByUsername(username);
	}
}
