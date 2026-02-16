package io.github.anderslunddev.pedalboard.security;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.model.BoardRepositoryAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OwnershipChecker {

	private final BoardRepositoryAdapter boardRepositoryAdapter;

	public OwnershipChecker(BoardRepositoryAdapter boardRepositoryAdapter) {
		this.boardRepositoryAdapter = boardRepositoryAdapter;
	}

	public boolean isCurrentUser(UserId ownerId) {
		return currentUserId().map(id -> id.equals(ownerId.value())).orElse(false);
	}

	public boolean ownsBoard(BoardId boardId) {
		Optional<Board> optBoard = boardRepositoryAdapter.findById(boardId);
		if (optBoard.isEmpty()) {
			// If the board doesn't exist, let the service handle 404 semantics
			return true;
		}
		UUID ownerId = optBoard.get().userId().value();
		return currentUserId().map(id -> id.equals(ownerId)).orElse(false);
	}

	private Optional<UUID> currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		Object details = authentication.getDetails();
		if (!(details instanceof UUID currentUserId)) {
			return Optional.empty();
		}
		return Optional.of(currentUserId);
	}
}
