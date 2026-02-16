package io.github.anderslunddev.pedalboard.security;

import io.github.anderslunddev.pedalboard.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

	private final Key key;
	private final long expirationSeconds;

	public JwtUtil(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException("JWT secret must be at least 32 characters");
		}
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder().setSubject(user.username()).setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
				.addClaims(Map.of("userId", user.id().toString())).signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public Jws<Claims> validate(String token) throws JwtException {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
	}
}
