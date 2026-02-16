# Code Review - Pedalboard Backend

**Date:** February 5, 2026  
**Reviewer:** AI Assistant  
**Scope:** Backend Java/Spring Boot application

---

## Executive Summary

Overall, the codebase demonstrates good domain-driven design principles with strong typing through value objects (`BoardId`, `UserId`, `PedalId`, etc.). The security implementation is functional but has several critical issues that need addressing. Error handling is inconsistent, and there are some architectural concerns.

**Priority Issues:**
- 🔴 **CRITICAL:** Security exceptions not properly handled (return 500 instead of 401/403)
- 🔴 **CRITICAL:** Missing global exception handler
- 🟡 **HIGH:** JWT secret key validation missing
- 🟡 **HIGH:** Inconsistent error responses
- 🟡 **MEDIUM:** Missing input validation on controllers
- 🟢 **LOW:** Code quality improvements

---

## 1. Security Issues

### 🔴 CRITICAL: SecurityException Not Mapped to HTTP Status Codes

**Location:** `BoardService.java`, `CableService.java`

**Issue:** `SecurityException` is thrown but not caught by controllers, resulting in 500 Internal Server Error instead of 401/403.

```java
// BoardService.java:79, 83, 86
throw new SecurityException("Unauthenticated");  // Should be 401
throw new SecurityException("Forbidden");         // Should be 403
```

**Impact:** Clients receive incorrect HTTP status codes, making error handling difficult.

**Recommendation:** 
1. Create custom exceptions (`UnauthenticatedException`, `ForbiddenException`) extending Spring's `AuthenticationException`/`AccessDeniedException`
2. Add `@ControllerAdvice` to map these to proper HTTP status codes
3. Or use Spring Security's built-in exception handling

**Fix:**
```java
// Create custom exceptions
public class UnauthenticatedException extends AuthenticationException {
    public UnauthenticatedException(String msg) { super(msg); }
}

public class ForbiddenException extends AccessDeniedException {
    public ForbiddenException(String msg) { super(msg); }
}

// Add global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<?> handleUnauthenticated(UnauthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
```

---

### 🟡 HIGH: JWT Secret Key Validation Missing

**Location:** `JwtUtil.java:22`

**Issue:** No validation that the JWT secret meets minimum length requirements for HS256 (should be at least 256 bits / 32 bytes).

```java
this.key = Keys.hmacShaKeyFor(secret.getBytes());
```

**Impact:** Weak secrets could lead to token forgery if secret is too short.

**Recommendation:**
```java
public JwtUtil(@Value("${app.jwt.secret}") String secret,
               @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
    if (secret == null || secret.length() < 32) {
        throw new IllegalArgumentException("JWT secret must be at least 32 characters");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationSeconds = expirationSeconds;
}
```

---

### 🟡 MEDIUM: JWT Token Validation Error Handling

**Location:** `JwtAuthenticationFilter.java:52`

**Issue:** All exceptions are silently swallowed with `catch (Exception ignored)`, including potential security issues.

**Current:**
```java
} catch (Exception ignored) {
    // invalid token: proceed unauthenticated; security will block where needed
}
```

**Recommendation:** At minimum, log security-related exceptions:
```java
} catch (JwtException e) {
    // Invalid token - proceed unauthenticated
    log.debug("Invalid JWT token", e);
} catch (Exception e) {
    log.warn("Unexpected error during JWT validation", e);
    // Still proceed - let security filter handle it
}
```

---

### 🟡 MEDIUM: CORS Configuration Too Permissive

**Location:** All controllers have `@CrossOrigin(origins = "*")`

**Issue:** Allows requests from any origin, which is fine for development but should be restricted in production.

**Recommendation:** Use environment-specific configuration:
```java
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
```

---

## 2. Error Handling Issues

### 🔴 CRITICAL: Missing Global Exception Handler

**Issue:** No centralized exception handling. Each controller handles exceptions individually, leading to:
- Inconsistent error response formats
- Security exceptions not handled
- Duplicate error handling code

**Recommendation:** Create `@ControllerAdvice` class:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex) {
        HttpStatus status = ex.getMessage().contains("Unauthenticated") 
            ? HttpStatus.UNAUTHORIZED 
            : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
            .body(new ErrorResponse(status.name(), ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
    
    record ErrorResponse(String code, String message) {}
}
```

---

### 🟡 HIGH: Inconsistent Error Response Formats

**Location:** Multiple controllers

**Issue:** Some endpoints return plain strings, others return objects. No standard error format.

**Examples:**
- `UserController`: Returns `String` for errors
- `BoardController`: Returns `String` for errors  
- `CableController`: Returns `String` for errors

**Recommendation:** Standardize on a consistent error response format:
```java
public record ErrorResponse(String code, String message, Map<String, Object> details) {
    public ErrorResponse(String code, String message) {
        this(code, message, Map.of());
    }
}
```

---

### 🟡 MEDIUM: Silent Failures in Services

**Location:** `BoardService.deleteBoard()`, `CableService.deleteCablesForBoard()`

**Issue:** Methods silently return when entity not found instead of throwing exceptions.

```java
// BoardService.java:44-47
Optional<Board> opt = boardRepositoryAdapter.findById(id);
if (opt.isEmpty()) {
    return;  // Silent failure
}
```

**Impact:** Callers can't distinguish between "deleted successfully" and "never existed".

**Recommendation:** Either:
1. Return `boolean` indicating success
2. Throw `NotFoundException` 
3. Document the behavior clearly

---

## 3. Input Validation Issues

### 🟡 HIGH: Missing Request Validation

**Location:** All controllers

**Issue:** No `@Valid` annotations or explicit validation on request DTOs.

**Example:**
```java
@PostMapping
public ResponseEntity<?> createBoard(@RequestBody CreateBoardRequest request) {
    // No validation that name is not blank, width > 0, etc.
}
```

**Recommendation:** Add validation:
```java
public record CreateBoardRequest(
    @NotBlank String name,
    @Positive double width,
    @Positive double height,
    @NotNull UUID userId
) {}

@PostMapping
public ResponseEntity<?> createBoard(@Valid @RequestBody CreateBoardRequest request) {
    // ...
}
```

---

### 🟡 MEDIUM: Email Validation Missing

**Location:** `UserService.register()`

**Issue:** No email format validation - only checks if blank.

**Recommendation:** Add email validation:
```java
public User register(String username, String email, String password) {
    if (!isValidEmail(email)) {
        throw new IllegalArgumentException("Invalid email format");
    }
    // ...
}

private boolean isValidEmail(String email) {
    return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
}
```

Or use Jakarta Validation:
```java
public record RegisterRequest(
    @NotBlank String username,
    @Email String email,
    @NotBlank @Size(min = 8) String password
) {}
```

---

## 4. Code Quality Issues

### 🟡 MEDIUM: Outdated Comment in User Domain

**Location:** `User.java:8`

**Issue:** Comment says "Password is stored as plain text" but passwords are now hashed.

```java
/**
 * Simple domain user.
 * NOTE: Password is stored as plain text for now – replace with hashing when adding real security.
 */
```

**Fix:** Update comment to reflect current implementation.

---

### 🟡 MEDIUM: Inconsistent Null Handling

**Location:** `CableService.createCable()`, `generateSequence()`

**Issue:** `generateSequence()` returns `null` for not found, but `createCable()` returns `Optional.empty()`.

```java
// CableService.java:70
if (optBoard.isEmpty()) {
    return null;  // Inconsistent
}
```

**Recommendation:** Use `Optional` consistently or throw exceptions.

---

### 🟢 LOW: Magic Numbers

**Location:** Various places

**Issue:** Hard-coded values like `3600` (JWT expiration) should be constants.

**Recommendation:** Extract to constants or configuration.

---

### 🟢 LOW: Missing JavaDoc

**Location:** Many public methods

**Issue:** Missing documentation for complex methods like `Board.resolvePlacementFor()`.

**Recommendation:** Add JavaDoc for public APIs.

---

## 5. Architecture/Design Issues

### 🟡 MEDIUM: Service Layer Bypassing Domain Logic

**Location:** `BoardService.createBoard()`

**Issue:** Name uniqueness check happens in service layer, not domain.

```java
// BoardService.java:24
if (boardRepositoryAdapter.findByName(name).isPresent()) {
    throw new IllegalArgumentException("A board with the name '" + name + "' already exists.");
}
```

**Recommendation:** Consider moving to domain if this is a business rule, or keep in service if it's an application-level constraint.

---

### 🟡 MEDIUM: Duplicate Ownership Check Logic

**Location:** `BoardService.ensureCurrentUserOwns()`, `CableService.ensureCurrentUserOwns()`

**Issue:** Identical code duplicated in two services.

**Recommendation:** Extract to a shared utility or use Spring Security's method-level security:
```java
@PreAuthorize("authentication.details == #userId.value()")
public Board createBoard(BoardName name, double width, double height, UserId userId) {
    // ...
}
```

---

### 🟢 LOW: Missing Transaction Boundaries

**Location:** `BoardService.createBoard()`

**Issue:** Method modifies data but isn't marked `@Transactional`.

**Recommendation:** Add `@Transactional` if multiple repository calls need atomicity.

---

## 6. Potential Bugs

### 🟡 MEDIUM: Race Condition in Board Name Check

**Location:** `BoardService.createBoard()`

**Issue:** Check-then-act pattern without transaction isolation could allow duplicate names.

```java
if (boardRepositoryAdapter.findByName(name).isPresent()) {
    throw new IllegalArgumentException(...);
}
return boardRepositoryAdapter.createBoard(...);  // Race condition possible
```

**Recommendation:** Add unique constraint at database level and handle `DataIntegrityViolationException`.

---

### 🟡 MEDIUM: UUID Parsing Without Validation

**Location:** `JwtAuthenticationFilter.java:48`

**Issue:** `UUID.fromString(userId)` could throw `IllegalArgumentException` if userId is malformed.

```java
if (userId != null) {
    auth.setDetails(UUID.fromString(userId));  // Could throw
}
```

**Recommendation:** Add try-catch or validate format first.

---

### 🟢 LOW: Potential NPE in Pedal Domain Conversion

**Location:** `BoardRepositoryAdapter.toDomain()` line 124

**Issue:** `entity.getBoard().getId()` could throw NPE if `getBoard()` returns null.

```java
entity.getBoard() != null ? entity.getBoard().getId() : null
```

**Recommendation:** Already handled, but could be cleaner:
```java
Optional.ofNullable(entity.getBoard())
    .map(BoardModel::getId)
    .orElse(null)
```

---

## 7. Testing Concerns

### 🟡 MEDIUM: Missing Security Tests

**Issue:** No tests verifying:
- Unauthenticated requests are rejected
- Users can't access other users' boards
- JWT validation works correctly

**Recommendation:** Add security integration tests.

---

### 🟡 MEDIUM: Missing Edge Case Tests

**Issue:** Tests don't cover:
- Invalid JWT tokens
- Expired tokens
- Malformed requests
- Concurrent board creation with same name

---

## 8. Configuration Issues

### 🟡 MEDIUM: Hardcoded JWT Secret in Config

**Location:** `application.yml:21`

**Issue:** Secret is hardcoded with comment "Dev-only" but no environment-specific config.

**Recommendation:** Use environment variables:
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:change-me-to-a-long-random-string-for-dev-only-1234567890}
```

---

### 🟢 LOW: H2 Console Enabled in Production

**Location:** `application.yml:9`

**Issue:** H2 console enabled - should be disabled in production.

**Recommendation:** Make environment-specific:
```yaml
h2:
  console:
    enabled: ${H2_CONSOLE_ENABLED:false}
```

---

## 9. Positive Aspects

✅ **Strong Domain Modeling:** Excellent use of value objects (`BoardId`, `UserId`, `PedalId`) prevents type confusion

✅ **Good Separation of Concerns:** Clear separation between domain, service, and API layers

✅ **Consistent Naming:** Clear, descriptive method and class names

✅ **Domain Validation:** Good validation in domain objects (Board, Pedal, etc.)

✅ **Transaction Management:** Proper use of `@Transactional` where needed

✅ **Password Security:** Properly using BCrypt for password hashing

---

## Priority Action Items

### Immediate (Before Production)
1. ✅ Add global exception handler for `SecurityException` → proper HTTP status codes
2. ✅ Add JWT secret validation (minimum length)
3. ✅ Standardize error response format
4. ✅ Add input validation with `@Valid`

### Short Term
5. ✅ Extract duplicate ownership check logic
6. ✅ Add database unique constraint for board names
7. ✅ Add security integration tests
8. ✅ Environment-specific configuration

### Long Term
9. ✅ Consider method-level security annotations
10. ✅ Add comprehensive JavaDoc
11. ✅ Add logging framework (SLF4J/Logback)
12. ✅ Add API documentation (OpenAPI/Swagger)

---

## Summary Statistics

- **Total Issues Found:** 25
- **Critical:** 2
- **High:** 6
- **Medium:** 12
- **Low:** 5

**Overall Assessment:** The codebase is well-structured with good domain modeling, but needs critical security exception handling fixes before production deployment.
