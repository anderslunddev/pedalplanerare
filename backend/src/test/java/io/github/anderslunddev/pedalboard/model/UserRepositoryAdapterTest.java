package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserModelConverter converter;

	@InjectMocks
	private UserRepositoryAdapter adapter;

	@Test
	void createUser_shouldConvertSaveAndReturnDomainUser() {
		String username = "alice";
		Email email = Email.parse("alice@example.com");
		String password = "secret";
		Role role = Role.USER;

		UserModel toSave = new UserModel();
		when(converter.toEntity(username, email, password, role)).thenReturn(toSave);

		UserModel savedModel = new UserModel();
		when(userRepository.save(toSave)).thenReturn(savedModel);

		User expectedUser = UserMother.withUsername(username);
		when(converter.toDomain(savedModel)).thenReturn(expectedUser);

		User result = adapter.createUser(username, email, password, role);

		assertSame(expectedUser, result);
		verify(converter).toEntity(username, email, password, role);
		verify(userRepository).save(toSave);
		verify(converter).toDomain(savedModel);
	}

	@Test
	void findByUsername_shouldReturnMappedUserWhenPresent() {
		String username = "bob";
		UserModel model = new UserModel();
		when(userRepository.findByUsername(username)).thenReturn(Optional.of(model));

		User expected = UserMother.withUsername(username);
		when(converter.toDomain(model)).thenReturn(expected);

		Optional<User> result = adapter.findByUsername(username);

		assertTrue(result.isPresent());
		assertSame(expected, result.get());
		verify(userRepository).findByUsername(username);
		verify(converter).toDomain(model);
	}

	@Test
	void findByUsername_shouldReturnEmptyWhenMissing() {
		String username = "nobody";
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

		Optional<User> result = adapter.findByUsername(username);

		assertTrue(result.isEmpty());
		verify(userRepository).findByUsername(username);
		verifyNoInteractions(converter);
	}

	@Test
	void existsByUsername_shouldReturnTrueWhenUserExists() {
		String username = "exists";
		when(userRepository.existsByUsername(username)).thenReturn(true);

		assertTrue(adapter.existsByUsername(username));
		verify(userRepository).existsByUsername(username);
	}

	@Test
	void existsByUsername_shouldReturnFalseWhenUserMissing() {
		String username = "missing";
		when(userRepository.existsByUsername(username)).thenReturn(false);

		assertFalse(adapter.existsByUsername(username));
		verify(userRepository).existsByUsername(username);
	}

	@Test
	void existsByEmail_shouldReturnTrueWhenEmailExists() {
		Email email = Email.parse("found@example.com");
		when(userRepository.existsByEmail(email.value())).thenReturn(true);

		assertTrue(adapter.existsByEmail(email));
		verify(userRepository).existsByEmail(email.value());
	}

	@Test
	void existsByEmail_shouldReturnFalseWhenEmailMissing() {
		Email email = Email.parse("none@example.com");
		when(userRepository.existsByEmail(email.value())).thenReturn(false);

		assertFalse(adapter.existsByEmail(email));
		verify(userRepository).existsByEmail(email.value());
	}
}
