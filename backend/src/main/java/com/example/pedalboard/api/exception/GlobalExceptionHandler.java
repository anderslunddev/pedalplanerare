package com.example.pedalboard.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Map SecurityException thrown from services (e.g. BoardService, CableService)
	 * to proper HTTP status codes instead of leaking 500 errors.
	 *
	 * Convention used in services: - message contains "Unauthenticated" -> 401
	 * UNAUTHORIZED - message contains "Forbidden" -> 403 FORBIDDEN
	 */
	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
		String message = ex.getMessage() != null ? ex.getMessage() : "";
		HttpStatus status;
		if (message.contains("Unauthenticated")) {
			status = HttpStatus.UNAUTHORIZED;
		} else if (message.contains("Forbidden")) {
			status = HttpStatus.FORBIDDEN;
		} else {
			status = HttpStatus.FORBIDDEN;
		}

		Map<String, Object> body = Map.of("status", status.value(), "error", status.getReasonPhrase(), "message",
				message);

		return ResponseEntity.status(status).body(body);
	}

	/**
	 * Handle validation errors from @Valid annotations on request DTOs. Returns
	 * detailed field-level validation errors.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(error.getField(), error.getDefaultMessage());
		}

		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.put("message", "Validation failed");
		body.put("fieldErrors", fieldErrors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Map Spring Security access-denied exceptions (from method security or
	 * filters) to a proper 403 response. Always return a generic message to
	 * avoid leaking internal details; log the actual message server-side only.
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		log.debug("Access denied: {}", ex.getMessage());
		HttpStatus status = HttpStatus.FORBIDDEN;
		Map<String, Object> body = Map.of("status", status.value(), "error", status.getReasonPhrase(), "message",
				"Forbidden");
		return ResponseEntity.status(status).body(body);
	}

	/**
	 * Handle validation and business rule violations. These are client errors (400
	 * Bad Request).
	 */
	@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(Exception ex) {
		String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";

		Map<String, Object> body = Map.of("status", HttpStatus.BAD_REQUEST.value(), "error",
				HttpStatus.BAD_REQUEST.getReasonPhrase(), "message", message);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Handle runtime exceptions that represent business logic errors. These are
	 * typically client errors (400 Bad Request) rather than server errors.
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
		// Check if this is a business logic error (IllegalStateException already
		// handled above)
		// or if it's something we should treat as a client error
		String message = ex.getMessage() != null ? ex.getMessage() : "Request processing failed";

		// Most RuntimeException in this codebase are business logic errors (e.g., from
		// CableService.generateSequence)
		// Treat as 400 unless it's clearly a server error
		Map<String, Object> body = Map.of("status", HttpStatus.BAD_REQUEST.value(), "error",
				HttpStatus.BAD_REQUEST.getReasonPhrase(), "message", message);

		log.debug("RuntimeException handled as client error", ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Catch-all for any unexpected exceptions. Logs the error and returns a generic
	 * 500 response.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		log.error("Unexpected error occurred", ex);

		Map<String, Object> body = Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(), "error",
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "message", "An unexpected error occurred");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
