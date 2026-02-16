package io.github.anderslunddev.pedalboard.service.cable;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.model.BoardRepositoryAdapter;
import io.github.anderslunddev.pedalboard.model.CableRepositoryAdapter;
import io.github.anderslunddev.pedalboard.model.PedalRepositoryAdapter;
import io.github.anderslunddev.pedalboard.service.cable.calculation.RoutingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CableService {

	private final CableRepositoryAdapter cableRepositoryAdapter;
	private final BoardRepositoryAdapter boardRepositoryAdapter;
	private final PedalRepositoryAdapter pedalRepositoryAdapter;
	private final RoutingService routingService;

	public CableService(CableRepositoryAdapter cableRepositoryAdapter, BoardRepositoryAdapter boardRepositoryAdapter,
			PedalRepositoryAdapter pedalRepositoryAdapter, RoutingService routingService) {
		this.cableRepositoryAdapter = cableRepositoryAdapter;
		this.boardRepositoryAdapter = boardRepositoryAdapter;
		this.pedalRepositoryAdapter = pedalRepositoryAdapter;
		this.routingService = routingService;
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public Optional<Cable> createCable(BoardId boardId, PedalId sourcePedalId, PedalId destinationPedalId) {
		Optional<Board> optBoard = boardRepositoryAdapter.findById(boardId);
		if (optBoard.isEmpty()) {
			return Optional.empty();
		}
		Board domainBoard = optBoard.get();

		Optional<Pedal> optSource = pedalRepositoryAdapter.findById(sourcePedalId);
		Optional<Pedal> optDestination = pedalRepositoryAdapter.findById(destinationPedalId);
		if (optSource.isEmpty() || optDestination.isEmpty()) {
			return Optional.empty();
		}
		Cable saved = createCable(optSource.get(), optDestination.get(), domainBoard, domainBoard.pedals());
		return Optional.of(saved);
	}

	@Transactional
	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public Optional<List<Cable>> generateSequence(BoardId boardId) {
		Optional<Board> optBoard = boardRepositoryAdapter.findById(boardId);
		if (optBoard.isEmpty()) {
			return Optional.empty();
		}

		Board domainBoard = optBoard.get();
		List<Pedal> pedals = domainBoard.pedals().stream()
				.sorted((a, b) -> Integer.compare(a.placement().value(), b.placement().value())).toList();

		if (pedals.isEmpty()) {
			cableRepositoryAdapter.deleteByBoardId(boardId);
			return Optional.of(List.of());
		}

		int expected = 1;
		for (Pedal p : pedals) {
			if (p.placement().value() != expected) {
				throw new IllegalStateException("Cannot connect pedals: Placement #" + expected + " is missing.");
			}
			expected++;
		}

		cableRepositoryAdapter.deleteByBoardId(boardId);

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
		Cable saved = cableRepositoryAdapter.saveCable(domainBoard.id(), source.id(), destination.id(),
				routing.pathPoints(), routingLength);
		return saved;
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public List<Cable> listCables(BoardId boardId) {
		Optional<Board> optBoard = boardRepositoryAdapter.findById(boardId);
		if (optBoard.isEmpty()) {
			return List.of();
		}
		return cableRepositoryAdapter.findByBoardId(boardId);
	}
}
