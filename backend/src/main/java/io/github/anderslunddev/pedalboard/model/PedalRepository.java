package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PedalRepository extends JpaRepository<PedalModel, UUID> {

	@Query("SELECT b.user.id FROM PedalModel p JOIN p.board b WHERE p.id = :pedalId")
	Optional<UUID> findBoardOwnerIdByPedalId(@Param("pedalId") UUID pedalId);
}
