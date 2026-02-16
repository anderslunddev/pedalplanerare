package com.example.pedalboard.security;

import com.example.pedalboard.domain.board.Board;
import com.example.pedalboard.domain.board.BoardId;
import com.example.pedalboard.domain.user.UserId;
import com.example.pedalboard.model.BoardRepositoryAdapter;
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
