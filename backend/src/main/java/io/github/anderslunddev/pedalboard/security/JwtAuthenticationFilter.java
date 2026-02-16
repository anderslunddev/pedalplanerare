package io.github.anderslunddev.pedalboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Jws<Claims> jws = jwtUtil.validate(token);
				String username = jws.getBody().getSubject();
				String userId = jws.getBody().get("userId", String.class);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails user = userDetailsService.loadUserByUsername(username);
					var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
					if (userId != null) {
						auth.setDetails(UUID.fromString(userId));
					}
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception ex) {
				// Invalid token: proceed unauthenticated; security will block where needed
				log.debug("JWT validation failed, proceeding unauthenticated", ex);
			}
		}

		filterChain.doFilter(request, response);
	}
}
