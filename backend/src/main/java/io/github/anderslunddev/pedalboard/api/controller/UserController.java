package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.domain.user.AuthPrincipal;
import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.security.JwtUtil;
import io.github.anderslunddev.pedalboard.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me() {
		Optional<UUID> userId = currentUserId();
		if (userId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return userService.findById(userId.get()).map(UserController::toResponse).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/me/password")
	public ResponseEntity<Void> changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request) {
		Optional<UUID> userId = currentUserId();
		if (userId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		userService.changeOwnPassword(userId.get(), request.currentPassword(), request.newPassword());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		try {
			var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
			authenticationManager.authenticate(authToken);

			User user = userService.findByUsername(request.username()).orElseThrow();

			String token = jwtUtil.generateToken(AuthPrincipal.fromUser(user));

			return ResponseEntity.ok(new LoginResponse(token, toResponse(user)));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	private static Optional<UUID> currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getDetails() instanceof UUID id)) {
			return Optional.empty();
		}
		return Optional.of(id);
	}

	private static UserResponse toResponse(User user) {
		return new UserResponse(user.id(), user.userName().value(), user.email().value(), user.role());
	}

	public record ChangePasswordRequest(
			@NotBlank(message = "Current password must not be blank") String currentPassword,
			@NotBlank(message = "New password must not be blank") @Size(min = 8, message = "New password must be at least 8 characters") String newPassword) {
	}

	public record LoginRequest(@NotBlank(message = "Username must not be blank") String username,
			@NotBlank(message = "Password must not be blank") String password) {
	}

	public record UserResponse(java.util.UUID id, String username, String email, Role role) {
	}

	public record LoginResponse(String token, UserResponse user) {
	}
}
