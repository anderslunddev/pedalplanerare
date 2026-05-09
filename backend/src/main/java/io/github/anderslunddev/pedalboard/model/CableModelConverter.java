package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.CableId;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Converter between {@link CableModel} and domain {@link Cable}.
 */
@Component
class CableModelConverter {

	Cable toDomain(CableModel entity) {
		Objects.requireNonNull(entity, "CableModel must not be null");

		List<io.github.anderslunddev.pedalboard.domain.cable.PathPoint> points = entity.getPathPoints().stream()
				.map(p -> new io.github.anderslunddev.pedalboard.domain.cable.PathPoint(p.getX(), p.getY()))
				.collect(Collectors.toList());

		Double lengthValue = entity.getTotalLength();
		if (lengthValue == null || lengthValue <= 0) {
			throw new IllegalStateException("Cable " + entity.getId() + " has invalid totalLength: " + lengthValue);
		}
		Length totalLength = new Length(lengthValue);

		return new Cable(new CableId(entity.getId()), new PedalId(entity.getSourcePedalId()),
				new PedalId(entity.getDestinationPedalId()), points, totalLength);
	}

	CableModel toEntity(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId,
			List<io.github.anderslunddev.pedalboard.domain.cable.PathPoint> pathPoints, Length totalLength) {
		Objects.requireNonNull(boardId, "BoardId must not be null");
		Objects.requireNonNull(sourcePedalId, "sourcePedalId must not be null");
		Objects.requireNonNull(destinationPedalId, "destinationPedalId must not be null");
		Objects.requireNonNull(pathPoints, "pathPoints must not be null");
		Objects.requireNonNull(totalLength, "Length must not be null");
		CableModel cable = new CableModel();
		cable.setBoardId(boardId.value());
		cable.setSourcePedalId(sourcePedalId.value());
		cable.setDestinationPedalId(destinationPedalId.value());
		cable.setPathPoints(pathPoints.stream().map(p -> new PathPoint(p.x(), p.y())).collect(Collectors.toList()));
		cable.setTotalLength(totalLength.value());
		return cable;
	}
}

