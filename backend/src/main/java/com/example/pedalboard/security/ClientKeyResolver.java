package com.example.pedalboard.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Resolves a client key for rate limiting (used by bucket4j-spring-boot-starter SpEL).
 * Prefers X-Forwarded-For when behind a proxy, otherwise remote address.
 */
@Component("clientKeyResolver")
public class ClientKeyResolver {

	public String getKey(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
	}
}
