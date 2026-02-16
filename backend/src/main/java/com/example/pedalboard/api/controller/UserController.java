package com.example.pedalboard.api.controller;

import com.example.pedalboard.domain.user.User;
import com.example.pedalboard.model.UserRepositoryAdapter;
import com.example.pedalboard.security.JwtUtil;
import com.example.pedalboard.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepositoryAdapter userRepositoryAdapter;

	public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UserRepositoryAdapter userRepositoryAdapter) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userRepositoryAdapter = userRepositoryAdapter;
	}

	@PostMapping
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		User created = userService.register(request.username(), request.email(), request.password());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new UserResponse(created.id(), created.username(), created.email()));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		try {
			var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
			authenticationManager.authenticate(authToken);

			User user = userRepositoryAdapter.findByUsername(request.username()).orElseThrow();

			String token = jwtUtil.generateToken(user);

			return ResponseEntity
					.ok(new LoginResponse(token, new UserResponse(user.id(), user.username(), user.email())));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	public record RegisterRequest(@NotBlank(message = "Username must not be blank") String username,
			@NotBlank(message = "Email must not be blank") @Email(message = "Email must be a valid email address") String email,
			@NotBlank(message = "Password must not be blank") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
	}

	public record LoginRequest(@NotBlank(message = "Username must not be blank") String username,
			@NotBlank(message = "Password must not be blank") String password) {
	}

	public record UserResponse(java.util.UUID id, String username, String email) {
	}

	public record LoginResponse(String token, UserResponse user) {
	}
}
