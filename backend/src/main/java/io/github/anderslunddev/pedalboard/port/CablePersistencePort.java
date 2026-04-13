package io.github.anderslunddev.pedalboard.port;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;

import java.util.List;

/**
 * Outgoing port: persist and query cable routes between pedals.
 */
public interface CablePersistencePort {

	Cable saveCable(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId, List<PathPoint> pathPoints,
			Length totalLength);

	List<Cable> findByBoardId(BoardId boardId);

	void deleteByBoardId(BoardId boardId);

	void deleteBySourcePedalIdOrDestinationPedalId(PedalId pedalId);

	void flush();
}
