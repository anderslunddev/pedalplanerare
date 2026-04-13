package io.github.anderslunddev.pedalboard.api.controller;

import io.github.anderslunddev.pedalboard.domain.user.Role;
import io.github.anderslunddev.pedalboard.domain.user.User;
import io.github.anderslunddev.pedalboard.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

	private final UserService userService;

	public AdminController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<AdminUserResponse> listUsers() {
		return userService.findAll().stream()
				.map(user -> new AdminUserResponse(user.id(), user.username(), user.email(), user.role())).toList();
	}

	@PostMapping
	public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		User created = userService.register(request.username(), request.email(), request.password(), request.role());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new AdminUserResponse(created.id(), created.username(), created.email(), created.role()));
	}

	@PutMapping("/{id}/role")
	public AdminUserResponse updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
		User updated = userService.updateRole(id, request.role());
		return new AdminUserResponse(updated.id(), updated.username(), updated.email(), updated.role());
	}

	@PutMapping("/{id}/password")
	public ResponseEntity<Void> resetPassword(@PathVariable UUID id,
			@Valid @RequestBody ResetPasswordRequest request) {
		userService.resetPassword(id, request.password());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	public record AdminUserResponse(UUID id, String username, String email, Role role) {
	}

	public record CreateUserRequest(
			@NotBlank(message = "Username must not be blank") String username,
			@NotBlank(message = "Email must not be blank") @Email(message = "Must be a valid email") String email,
			@NotBlank(message = "Password must not be blank") @Size(min = 8, message = "Password must be at least 8 characters") String password,
			Role role) {
		public CreateUserRequest {
			if (role == null) {
				role = Role.USER;
			}
		}
	}

	public record UpdateRoleRequest(@NotNull(message = "Role must not be null") Role role) {
	}

	public record ResetPasswordRequest(
			@NotBlank(message = "Password must not be blank") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
	}
}
