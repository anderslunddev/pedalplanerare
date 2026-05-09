package io.github.anderslunddev.pedalboard.service.user;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.user.UserName;
import io.github.anderslunddev.pedalboard.port.UserPersistencePort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private final UserPersistencePort userPersistence;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserPersistencePort userPersistence, PasswordEncoder passwordEncoder) {
		this.userPersistence = userPersistence;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User register(String username, String emailRaw, String password) {
		return register(username, emailRaw, password, Role.USER);
	}

	@Transactional
	public User register(String usernameRaw, String emailRaw, String password, Role role) {
		UserName userName = UserName.parse(usernameRaw);
		Email email = Email.parse(emailRaw);
		assertUsernameAndEmailAvailable(userName, email);
		String hashed = passwordEncoder.encode(password);
		return userPersistence.createUser(userName, email, hashed, role);
	}

	private void assertUsernameAndEmailAvailable(UserName userName, Email email) {
		if (userPersistence.existsByUsername(userName)) {
			throw new IllegalArgumentException("Username is already taken");
		}
		if (userPersistence.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}
	}

	public Optional<User> findByUsername(String rawUsername) {
		try {
			return userPersistence.findByUsername(UserName.parse(rawUsername));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public Optional<User> findById(UserId id) {
		return userPersistence.findById(id);
	}

	public List<User> findAll() {
		return userPersistence.findAll();
	}

	@Transactional
	public User updateRole(UserId userId, Role role) {
		return userPersistence.updateRole(userId, role);
	}

	@Transactional
	public User resetPassword(UserId userId, String newPassword) {
		String hashed = passwordEncoder.encode(newPassword);
		return userPersistence.updatePassword(userId, hashed);
	}

	@Transactional
	public void deleteUser(UserId userId) {
		userPersistence.deleteById(userId);
	}

	@Transactional
	public void changeOwnPassword(UserId userId, String currentPassword, String newPassword) {
		User user = userPersistence.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (!passwordEncoder.matches(currentPassword, user.password())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		userPersistence.updatePassword(userId, passwordEncoder.encode(newPassword));
	}
}
