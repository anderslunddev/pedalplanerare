package io.github.anderslunddev.pedalboard.service.board;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.port.BoardPersistencePort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BoardService {

	private final BoardPersistencePort boardPersistence;

	public BoardService(BoardPersistencePort boardPersistence) {
		this.boardPersistence = boardPersistence;
	}

	@PreAuthorize("@ownershipChecker.isCurrentUser(#userId)")
	public Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId) {
		if (boardPersistence.findByNameAndUserId(name, userId).isPresent()) {
			throw new IllegalArgumentException("You already have a board named '" + name.value() + "'.");
		}
		return boardPersistence.createBoard(name, surfaceArea, userId);
	}

	@PreAuthorize("@ownershipChecker.isCurrentUser(#userId)")
	public java.util.List<Board> getBoardsForUser(UserId userId) {
		return boardPersistence.findByUserId(userId);
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#id)")
	public Optional<Board> getBoard(BoardId id) {
		return boardPersistence.findById(id);
	}

	@Transactional
	@PreAuthorize("@ownershipChecker.ownsBoard(#id)")
	public void deleteBoard(BoardId id) {
		Optional<Board> opt = boardPersistence.findById(id);
		if (opt.isEmpty()) {
			return;
		}
		boardPersistence.deleteBoard(id);
	}

	/**
	 * Add a pedal to a board using pure domain rules.
	 *
	 * @return Optional.empty() if the board does not exist
	 * @throws IllegalArgumentException
	 *             if the requested placement is invalid (e.g. duplicate)
	 */
	@Transactional
	@PreAuthorize("@ownershipChecker.ownsBoard(#boardId)")
	public Optional<Pedal> addPedalToBoard(BoardId boardId, PedalToCreate pedalToCreate) {
		Optional<Board> boardOpt = boardPersistence.findById(boardId);
		if (boardOpt.isEmpty()) {
			return Optional.empty();
		}

		Board board = boardOpt.get();
		if (board.wouldOverlapWithExisting(pedalToCreate)) {
			throw new IllegalArgumentException("Pedal would overlap an existing pedal. Choose a different position.");
		}
		Placement placement = board.resolvePlacementFor(pedalToCreate);

		return boardPersistence.addPedalToBoard(boardId, pedalToCreate, placement);
	}

}
