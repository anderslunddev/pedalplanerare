package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.user.Email;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserMother;
import io.github.anderslunddev.pedalboard.domain.user.UserName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
		UserName userName = UserName.parse("alice");
		Email email = Email.parse("alice@example.com");
		String password = "secret";
		Role role = Role.USER;

		UserModel toSave = new UserModel();
		when(converter.toEntity(userName, email, password, role)).thenReturn(toSave);

		UserModel savedModel = new UserModel();
		when(userRepository.save(toSave)).thenReturn(savedModel);

		User expectedUser = UserMother.withUserName("alice");
		when(converter.toDomain(savedModel)).thenReturn(expectedUser);

		User result = adapter.createUser(userName, email, password, role);

		assertSame(expectedUser, result);
		verify(converter).toEntity(userName, email, password, role);
		verify(userRepository).save(toSave);
		verify(converter).toDomain(savedModel);
	}

	@Test
	void findByUsername_shouldReturnMappedUserWhenPresent() {
		UserName userName = UserName.parse("bob");
		UserModel model = new UserModel();
		when(userRepository.findByUsername(userName.value())).thenReturn(Optional.of(model));

		User expected = UserMother.withUserName("bob");
		when(converter.toDomain(model)).thenReturn(expected);

		Optional<User> result = adapter.findByUsername(userName);

		assertTrue(result.isPresent());
		assertSame(expected, result.get());
		verify(userRepository).findByUsername(userName.value());
		verify(converter).toDomain(model);
	}

	@Test
	void findByUsername_shouldReturnEmptyWhenMissing() {
		UserName userName = UserName.parse("nobody");
		when(userRepository.findByUsername(userName.value())).thenReturn(Optional.empty());

		Optional<User> result = adapter.findByUsername(userName);

		assertTrue(result.isEmpty());
		verify(userRepository).findByUsername(userName.value());
		verifyNoInteractions(converter);
	}

	@Test
	void existsByUsername_shouldReturnTrueWhenUserExists() {
		UserName userName = UserName.parse("exists");
		when(userRepository.existsByUsername(userName.value())).thenReturn(true);

		assertTrue(adapter.existsByUsername(userName));
		verify(userRepository).existsByUsername(userName.value());
	}

	@Test
	void existsByUsername_shouldReturnFalseWhenUserMissing() {
		UserName userName = UserName.parse("missing");
		when(userRepository.existsByUsername(userName.value())).thenReturn(false);

		assertFalse(adapter.existsByUsername(userName));
		verify(userRepository).existsByUsername(userName.value());
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
