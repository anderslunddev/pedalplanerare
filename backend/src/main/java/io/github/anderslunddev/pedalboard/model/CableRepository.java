package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CableRepository extends JpaRepository<CableModel, UUID> {

	List<CableModel> findByBoardId(UUID boardId);

	void deleteByBoardId(UUID boardId);

	void deleteBySourcePedalIdOrDestinationPedalId(UUID sourcePedalId, UUID destinationPedalId);
}
