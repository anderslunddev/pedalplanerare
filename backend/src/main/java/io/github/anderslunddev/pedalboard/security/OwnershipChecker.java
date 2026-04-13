package io.github.anderslunddev.pedalboard.security;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.port.BoardPersistencePort;
import io.github.anderslunddev.pedalboard.port.PedalPersistencePort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OwnershipChecker {

	private final BoardPersistencePort boardPersistence;
	private final PedalPersistencePort pedalPersistence;

	public OwnershipChecker(BoardPersistencePort boardPersistence, PedalPersistencePort pedalPersistence) {
		this.boardPersistence = boardPersistence;
		this.pedalPersistence = pedalPersistence;
	}

	public boolean isCurrentUser(UserId ownerId) {
		return currentUserId().map(id -> id.equals(ownerId.value())).orElse(false);
	}

	public boolean ownsBoard(BoardId boardId) {
		Optional<Board> optBoard = boardPersistence.findById(boardId);
		if (optBoard.isEmpty()) {
			return true;
		}
		UUID ownerId = optBoard.get().userId().value();
		return currentUserId().map(id -> id.equals(ownerId)).orElse(false);
	}

	/**
	 * Checks ownership via a scalar query to avoid loading PedalModel/BoardModel
	 * into the persistence context, which would interfere with subsequent deletes
	 * due to OpenEntityManagerInView and Hibernate's orphanRemoval.
	 */
	public boolean ownsPedalById(UUID pedalId) {
		Optional<UUID> ownerIdOpt = pedalPersistence.findBoardOwnerId(new PedalId(pedalId));
		if (ownerIdOpt.isEmpty()) {
			return true;
		}
		return currentUserId().map(id -> id.equals(ownerIdOpt.get())).orElse(false);
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
