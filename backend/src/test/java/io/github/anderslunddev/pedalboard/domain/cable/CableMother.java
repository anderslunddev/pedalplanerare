package io.github.anderslunddev.pedalboard.domain.cable;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;

import java.util.List;
import java.util.UUID;

/**
 * Object Mother for {@link Cable} to simplify record construction in tests.
 */
public final class CableMother {

	private CableMother() {
	}

	public static Cable simple() {
		return new Cable(
				new CableId(UUID.randomUUID()),
				new BoardId(UUID.randomUUID()),
				new PedalId(UUID.randomUUID()),
				new PedalId(UUID.randomUUID()),
				List.of(new PathPoint(0.0, 0.0), new PathPoint(10.0, 10.0)),
				new Length(14.14));
	}

	public static Cable withBoardId(BoardId boardId) {
		Cable base = simple();
		return new Cable(base.id(), boardId, base.sourcePedalId(), base.destinationPedalId(),
				base.pathPoints(), base.totalLength());
	}

	public static Cable withSourceAndDestination(PedalId sourcePedalId, PedalId destinationPedalId) {
		Cable base = simple();
		return new Cable(base.id(), base.boardId(), sourcePedalId, destinationPedalId,
				base.pathPoints(), base.totalLength());
	}
}
