package io.github.anderslunddev.pedalboard.service.cable;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.port.BoardPersistencePort;
import io.github.anderslunddev.pedalboard.port.CablePersistencePort;
import io.github.anderslunddev.pedalboard.port.PedalPersistencePort;
import io.github.anderslunddev.pedalboard.service.cable.calculation.RoutingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CableService {

	private final CablePersistencePort cablePersistence;
	private final BoardPersistencePort boardPersistence;
	private final PedalPersistencePort pedalPersistence;
	private final RoutingService routingService;

	public CableService(CablePersistencePort cablePersistence, BoardPersistencePort boardPersistence,
			PedalPersistencePort pedalPersistence, RoutingService routingService) {
		this.cablePersistence = cablePersistence;
		this.boardPersistence = boardPersistence;
		this.pedalPersistence = pedalPersistence;
		this.routingService = routingService;
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public Optional<Cable> createCable(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId) {
		Optional<Board> optBoard = boardPersistence.findById(boardId);
		if (optBoard.isEmpty()) {
			return Optional.empty();
		}
		Board domainBoard = optBoard.get();

		Optional<Pedal> optSource = pedalPersistence.findById(sourcePedalId);
		Optional<Pedal> optDestination = pedalPersistence.findById(destinationPedalId);
		if (optSource.isEmpty() || optDestination.isEmpty()) {
			return Optional.empty();
		}
		Cable saved = createCable(optSource.get(), optDestination.get(), domainBoard, domainBoard.pedals());
		return Optional.of(saved);
	}

	@Transactional
	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public Optional<List<Cable>> generateSequence(BoardId boardId) {
		Optional<Board> optBoard = boardPersistence.findById(boardId);
		if (optBoard.isEmpty()) {
			return Optional.empty();
		}

		Board domainBoard = optBoard.get();
		List<Pedal> pedals = domainBoard.pedals().stream()
				.sorted((a, b) -> Integer.compare(a.placement().value(), b.placement().value())).toList();

		if (pedals.isEmpty()) {
			cablePersistence.deleteByBoardId(boardId);
			return Optional.of(List.of());
		}
		if (pedals.size() == 1) {
			cablePersistence.deleteByBoardId(boardId);
			return Optional.of(List.of());
		}

		cablePersistence.deleteByBoardId(boardId);

		List<Cable> result = new ArrayList<>();

		for (int i = 0; i < pedals.size() - 1; i++) {
			Cable saved = createCable(pedals.get(i), pedals.get(i + 1), domainBoard, pedals);
			result.add(saved);
		}

		return Optional.of(result);
	}

	private Cable createCable(Pedal source, Pedal destination, Board domainBoard, List<Pedal> pedals) {

		RoutingService.RoutingResult routing = routingService.route(domainBoard, source, destination, pedals);

		Length routingLength = new Length(routing.totalLength());
		return cablePersistence.saveCable(domainBoard.id(), source.id(), destination.id(), routing.pathPoints(),
				routingLength);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteCablesForPedal(PedalId pedalId) {
		cablePersistence.deleteBySourcePedalIdOrDestinationPedalId(pedalId);
		cablePersistence.flush();
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public List<Cable> listCables(BoardId boardId) {
		Optional<Board> optBoard = boardPersistence.findById(boardId);
		if (optBoard.isEmpty()) {
			return List.of();
		}
		return cablePersistence.findByBoardId(boardId);
	}
}
