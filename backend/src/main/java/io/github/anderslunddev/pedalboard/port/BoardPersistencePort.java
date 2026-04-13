package io.github.anderslunddev.pedalboard.port;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;

import java.util.List;
import java.util.Optional;

/**
 * Outgoing port: load and persist boards and pedals on boards.
 */
public interface BoardPersistencePort {

	Optional<Board> findByName(BoardName name);

	Optional<Board> findByNameAndUserId(BoardName name, UserId userId);

	List<Board> findByUserId(UserId userId);

	Optional<Board> findById(BoardId id);

	Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId);

	void deleteBoard(BoardId id);

	Optional<Pedal> addPedalToBoard(BoardId boardId, PedalToCreate pedalToCreate, Placement placement);
}
