package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

	Optional<UserModel> findByUsername(String username);

	Optional<UserModel> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
