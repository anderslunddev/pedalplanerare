package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CableRepository extends JpaRepository<CableModel, UUID> {

	List<CableModel> findByBoardId(UUID boardId);

	@Query("SELECT c FROM CableModel c WHERE c.sourcePedalId = :pedalId OR c.destinationPedalId = :pedalId")
	List<CableModel> findBySourcePedalIdOrDestinationPedalId(@Param("pedalId") UUID pedalId);
}
