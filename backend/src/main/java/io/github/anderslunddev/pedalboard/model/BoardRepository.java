package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<BoardModel, UUID> {
	Optional<BoardModel> findByName(String name);

	Optional<BoardModel> findByNameAndUser_Id(String name, UUID userId);

	List<BoardModel> findByUserId(UUID userId);
}
