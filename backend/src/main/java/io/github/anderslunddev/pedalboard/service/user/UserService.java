package io.github.anderslunddev.pedalboard.service.user;

import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.port.UserPersistencePort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

	private final UserPersistencePort userPersistence;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserPersistencePort userPersistence, PasswordEncoder passwordEncoder) {
		this.userPersistence = userPersistence;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User register(String username, String email, String password) {
		return register(username, email, password, Role.USER);
	}

	@Transactional
	public User register(String username, String email, String password, Role role) {
		if (userPersistence.existsByUsername(username)) {
			throw new IllegalArgumentException("Username is already taken");
		}
		if (userPersistence.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}
		String hashed = passwordEncoder.encode(password);
		return userPersistence.createUser(username, email, hashed, role);
	}

	public Optional<User> findByUsername(String username) {
		return userPersistence.findByUsername(username);
	}

	public Optional<User> findById(UUID id) {
		return userPersistence.findById(id);
	}

	public List<User> findAll() {
		return userPersistence.findAll();
	}

	@Transactional
	public User updateRole(UUID userId, Role role) {
		return userPersistence.updateRole(userId, role);
	}

	@Transactional
	public User resetPassword(UUID userId, String newPassword) {
		String hashed = passwordEncoder.encode(newPassword);
		return userPersistence.updatePassword(userId, hashed);
	}

	@Transactional
	public void deleteUser(UUID userId) {
		userPersistence.deleteById(userId);
	}

	@Transactional
	public User promoteToAdmin(String username) {
		User user = userPersistence.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		if (user.isAdmin()) {
			return user;
		}
		return userPersistence.updateRole(user.id(), Role.ADMIN);
	}
}
