package com.example.pedalboard.security;

import com.example.pedalboard.domain.user.User;
import com.example.pedalboard.model.UserRepositoryAdapter;
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
		User u = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		return org.springframework.security.core.userdetails.User.withUsername(u.username()).password(u.password())
				.roles("USER").build();
	}
}
