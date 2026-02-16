package com.example.pedalboard.service.board;

import com.example.pedalboard.domain.board.Board;
import com.example.pedalboard.domain.board.BoardId;
import com.example.pedalboard.domain.board.BoardName;
import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalToCreate;
import com.example.pedalboard.domain.pedal.Placement;
import com.example.pedalboard.domain.user.UserId;
import com.example.pedalboard.domain.value.SurfaceArea;
import com.example.pedalboard.model.BoardRepositoryAdapter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BoardService {

	private final BoardRepositoryAdapter boardRepositoryAdapter;

	public BoardService(BoardRepositoryAdapter boardRepositoryAdapter) {
		this.boardRepositoryAdapter = boardRepositoryAdapter;
	}

	@PreAuthorize("@ownershipChecker.isCurrentUser(#userId)")
	public Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId) {
		// Check if name already exists
		if (boardRepositoryAdapter.findByName(name).isPresent()) {
			throw new IllegalArgumentException("A board with the name '" + name + "' already exists.");
		}
		return boardRepositoryAdapter.createBoard(name, surfaceArea, userId);
	}

	@PreAuthorize("@ownershipChecker.isCurrentUser(#userId)")
	public java.util.List<Board> getBoardsForUser(UserId userId) {
		return boardRepositoryAdapter.findByUserId(userId);
	}

	@PreAuthorize("@ownershipChecker.ownsBoard(#id)")
	public Optional<Board> getBoard(BoardId id) {
		return boardRepositoryAdapter.findById(id);
	}

	@Transactional
	@PreAuthorize("@ownershipChecker.ownsBoard(#id)")
	public void deleteBoard(BoardId id) {
		Optional<Board> opt = boardRepositoryAdapter.findById(id);
		if (opt.isEmpty()) {
			return;
		}
		boardRepositoryAdapter.deleteBoard(id);
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
		// Ensure board exists
		Optional<Board> boardOpt = boardRepositoryAdapter.findById(boardId);
		if (boardOpt.isEmpty()) {
			return Optional.empty();
		}

		Board board = boardOpt.get();
		// Domain logic: decide final placement
		Placement placement = board.resolvePlacementFor(pedalToCreate);

		// Persist and return created pedal
		return boardRepositoryAdapter.addPedalToBoard(boardId, pedalToCreate, placement);
	}

}
