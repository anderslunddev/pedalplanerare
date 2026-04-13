package io.github.anderslunddev.pedalboard.security;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.model.UserRepositoryAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepositoryAdapter users;

	public CustomUserDetailsService(UserRepositoryAdapter users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		return org.springframework.security.core.userdetails.User.withUsername(user.username()).password(user.password())
				.roles(user.role()).build();
	}
}
