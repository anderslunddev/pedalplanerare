package io.github.anderslunddev.pedalboard.service.user;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.model.UserRepositoryAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
		return register(username, email, password, User.ROLE_USER);
	}

	@Transactional
	public User register(String username, String email, String password, String role) {
		if (userRepositoryAdapter.existsByUsername(username)) {
			throw new IllegalArgumentException("Username is already taken");
		}
		if (userRepositoryAdapter.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}
		String hashed = passwordEncoder.encode(password);
		return userRepositoryAdapter.createUser(username, email, hashed, role);
	}

	public Optional<User> findByUsername(String username) {
		return userRepositoryAdapter.findByUsername(username);
	}

	public Optional<User> findById(UUID id) {
		return userRepositoryAdapter.findById(id);
	}

	public List<User> findAll() {
		return userRepositoryAdapter.findAll();
	}

	@Transactional
	public User updateRole(UUID userId, String role) {
		if (!User.ROLE_USER.equals(role) && !User.ROLE_ADMIN.equals(role)) {
			throw new IllegalArgumentException("Invalid role: " + role);
		}
		return userRepositoryAdapter.updateRole(userId, role);
	}

	@Transactional
	public User resetPassword(UUID userId, String newPassword) {
		String hashed = passwordEncoder.encode(newPassword);
		return userRepositoryAdapter.updatePassword(userId, hashed);
	}

	@Transactional
	public void deleteUser(UUID userId) {
		userRepositoryAdapter.deleteById(userId);
	}

	@Transactional
	public User promoteToAdmin(String username) {
		User user = userRepositoryAdapter.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		if (user.isAdmin()) {
			return user;
		}
		return userRepositoryAdapter.updateRole(user.id(), User.ROLE_ADMIN);
	}
}
