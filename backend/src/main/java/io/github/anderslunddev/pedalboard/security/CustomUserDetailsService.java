package io.github.anderslunddev.pedalboard.security;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.domain.user.UserName;
import io.github.anderslunddev.pedalboard.port.UserPersistencePort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserPersistencePort users;

	public CustomUserDetailsService(UserPersistencePort users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final UserName userName;
		try {
			userName = UserName.parse(username);
		} catch (IllegalArgumentException e) {
			throw new UsernameNotFoundException(username);
		}
		User user = users.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException(username));
		return org.springframework.security.core.userdetails.User.withUsername(user.userName().value())
				.password(user.password())
				.roles(user.role().name()).build();
	}
}
