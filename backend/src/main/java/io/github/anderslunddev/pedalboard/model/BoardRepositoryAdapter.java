package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardRepositoryAdapter {

	private final BoardRepository boardRepository;
	private final PedalRepository pedalRepository;
	private final UserRepository userRepository;
	private final BoardModelConverter converter;

	public BoardRepositoryAdapter(BoardRepository boardRepository, PedalRepository pedalRepository,
                                  UserRepository userRepository, BoardModelConverter converter) {
		this.boardRepository = boardRepository;
		this.pedalRepository = pedalRepository;
		this.userRepository = userRepository;
        this.converter = converter;
    }

	public Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId) {
		UserModel user = userRepository.findById(userId.value())
				.orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));
		BoardModel saved = boardRepository
				.save(new BoardModel(name.value(), surfaceArea.width(), surfaceArea.height(), user));
		return converter.toDomain(saved);
	}

	public Optional<Board> findByName(BoardName name) {
		return boardRepository.findByName(name.value()).map(converter::toDomain);
	}

	public List<Board> findByUserId(UserId userId) {
		return boardRepository.findByUserId(userId.value()).stream().map(converter::toDomain).toList();
	}

	public Optional<Board> findById(BoardId id) {
		return boardRepository.findById(id.value()).map(converter::toDomain);
	}

	public void deleteBoard(BoardId id) {
		boardRepository.deleteById(id.value());
	}

	/**
	 * Persist a new pedal on a board using a pre-resolved placement.
	 *
	 * Domain rules (such as placement uniqueness) MUST be applied before calling
	 * this method.
	 */
	public Optional<Pedal> addPedalToBoard(BoardId boardId, PedalToCreate pedalToCreate, Placement placement) {
		Optional<BoardModel> optBoard = boardRepository.findById(boardId.value());
		if (optBoard.isEmpty()) {
			return Optional.empty();
		}
		BoardModel board = optBoard.get();

		PedalModel pedal = new PedalModel();
		pedal.setBoard(board);
		pedal.setName(pedalToCreate.getName().value());
		pedal.setWidth(pedalToCreate.getSurfaceArea().width());
		pedal.setHeight(pedalToCreate.getSurfaceArea().height());
		pedal.setColor(pedalToCreate.getColor().value());
		pedal.setX(pedalToCreate.getCoordinate().x());
		pedal.setY(pedalToCreate.getCoordinate().y());
		pedal.setPlacement(placement.value());

		PedalModel saved = pedalRepository.save(pedal);
		return Optional.of(converter.toDomain(saved));
	}




}
