package com.example.pedalboard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;
	private final String allowedOrigins;
	private final boolean h2ConsoleEnabled;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter,
			@Value("${app.cors.allowed-origins:*}") String allowedOrigins,
			@Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) {
		this.jwtFilter = jwtFilter;
		this.allowedOrigins = allowedOrigins;
		this.h2ConsoleEnabled = h2ConsoleEnabled;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder,
			UserDetailsService uds) throws Exception {
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.userDetailsService(uds).passwordEncoder(encoder);
		return builder.build();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		if (h2ConsoleEnabled) {
			http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		}
		http.authorizeHttpRequests(auth -> {
			auth.requestMatchers("/", "/index.html", "/assets/**", "/static/**", "/favicon.ico",
					"/api/users/login", "/api/users").permitAll();
			if (h2ConsoleEnabled) {
				auth.requestMatchers("/h2-console/**").permitAll();
			}
			auth.anyRequest().authenticated();
		}).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.toList();

		// Only allow * or explicit origins; empty list (e.g. prod without CORS_ALLOWED_ORIGINS) allows no origin
		if (!origins.isEmpty()) {
			origins.forEach(configuration::addAllowedOriginPattern);
		}

		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
