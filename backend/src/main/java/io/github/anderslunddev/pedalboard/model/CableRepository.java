package io.github.anderslunddev.pedalboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CableRepository extends JpaRepository<CableModel, UUID> {

	List<CableModel> findByBoardId(UUID boardId);
//TODO code smell regarding all sql
	/** Deletes path points for all cables on the board (path points first for FK). */
	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM cable_path_point WHERE cable_id IN (SELECT id FROM cable_model WHERE board_id = :boardId)", nativeQuery = true)
	void deletePathPointsByBoardId(@Param("boardId") UUID boardId);

	/** Deletes all cables for the board. */
	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM cable_model WHERE board_id = :boardId", nativeQuery = true)
	void deleteCablesByBoardId(@Param("boardId") UUID boardId);

	/** Deletes path points for cables that reference the given pedal (path points first for FK). */
	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM cable_path_point WHERE cable_id IN (SELECT id FROM cable_model WHERE source_pedal_id = :pedalId OR destination_pedal_id = :pedalId)", nativeQuery = true)
	void deletePathPointsByPedalId(@Param("pedalId") UUID pedalId);

	/** Deletes cables that reference the given pedal as source or destination. */
	@Modifying(clearAutomatically = true)
	@Query(value = "DELETE FROM cable_model WHERE source_pedal_id = :pedalId OR destination_pedal_id = :pedalId", nativeQuery = true)
	void deleteCablesByPedalId(@Param("pedalId") UUID pedalId);
}
