package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.CableId;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CableRepositoryAdapter {

	private final CableRepository cableRepository;

	public CableRepositoryAdapter(CableRepository cableRepository) {
		this.cableRepository = cableRepository;
	}

	public Cable saveCable(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId,
			List<io.github.anderslunddev.pedalboard.domain.cable.PathPoint> pathPoints, Length totalLength) {
		CableModel cable = new CableModel();
		cable.setBoardId(boardId.value());
		cable.setSourcePedalId(sourcePedalId.value());
		cable.setDestinationPedalId(destinationPedalId.value());
		cable.setPathPoints(pathPoints.stream().map(p -> new PathPoint(p.x(), p.y())).collect(Collectors.toList()));
		cable.setTotalLength(totalLength.value());

		CableModel saved = cableRepository.save(cable);
		return toDomain(saved);
	}

	public List<Cable> findByBoardId(BoardId boardId) {
		return cableRepository.findByBoardId(boardId.value()).stream().map(CableRepositoryAdapter::toDomain).toList();
	}

	public void deleteByBoardId(BoardId boardId) {
		List<CableModel> cables = cableRepository.findByBoardId(boardId.value());
		cableRepository.deleteAll(cables);
	}

	public void deleteBySourcePedalIdOrDestinationPedalId(PedalId pedalId) {
		List<CableModel> cables = cableRepository.findBySourcePedalIdOrDestinationPedalId(pedalId.value());
		cableRepository.deleteAll(cables);
	}

	public void flush() {
		cableRepository.flush();
	}

	private static Cable toDomain(CableModel entity) {
		if (entity == null) //TODO implicit logic in null return. bad.
			return null;
		List<io.github.anderslunddev.pedalboard.domain.cable.PathPoint> points = entity.getPathPoints().stream()
				.map(p -> new io.github.anderslunddev.pedalboard.domain.cable.PathPoint(p.getX(), p.getY()))
				.collect(Collectors.toList());

		Double lengthValue = entity.getTotalLength();
		if (lengthValue == null || lengthValue <= 0) {
			throw new IllegalStateException("Cable " + entity.getId() + " has invalid totalLength: " + lengthValue);
		}
		Length totalLength = new Length(lengthValue);

		return new Cable(new CableId(entity.getId()), new BoardId(entity.getBoardId()),
				new PedalId(entity.getSourcePedalId()), new PedalId(entity.getDestinationPedalId()), points,
				totalLength);
	}
}
