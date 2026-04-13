package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.port.CablePersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CableRepositoryAdapter implements CablePersistencePort {

	private final CableRepository cableRepository;
	private final CableModelConverter converter;

	public CableRepositoryAdapter(CableRepository cableRepository, CableModelConverter converter) {
		this.cableRepository = cableRepository;
		this.converter = converter;
	}

	public Cable saveCable(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId,
			List<PathPoint> pathPoints, Length totalLength) {
		CableModel toSave = converter.toEntity(boardId, sourcePedalId, destinationPedalId, pathPoints, totalLength);
		CableModel saved = cableRepository.save(toSave);
		return converter.toDomain(saved);
	}

	public List<Cable> findByBoardId(BoardId boardId) {
		return cableRepository.findByBoardId(boardId.value()).stream().map(converter::toDomain).toList();
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
}
