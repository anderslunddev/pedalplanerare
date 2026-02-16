package io.github.anderslunddev.pedalboard.domain.cable;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;

import java.util.List;
import java.util.Objects;

public record Cable(CableId id, BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId,
		List<PathPoint> pathPoints, Length totalLength) {

	public Cable(CableId id, BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId,
			List<PathPoint> pathPoints, Length totalLength) {
		this.id = Objects.requireNonNull(id, "Cable id must not be null");
		this.boardId = Objects.requireNonNull(boardId, "Cable boardId must not be null");
		this.sourcePedalId = Objects.requireNonNull(sourcePedalId, "Cable sourcePedalId must not be null");
		this.destinationPedalId = Objects.requireNonNull(destinationPedalId,
				"Cable destinationPedalId must not be null");
		List<PathPoint> pts = Objects.requireNonNull(pathPoints, "Cable pathPoints must not be null");
		if (pts.isEmpty()) {
			throw new IllegalArgumentException("Cable pathPoints must not be empty");
		}
		this.pathPoints = List.copyOf(pts);
		this.totalLength = Objects.requireNonNull(totalLength, "Cable totalLength must not be null");
	}
}
