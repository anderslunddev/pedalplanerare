package com.example.pedalboard.model;

import com.example.pedalboard.domain.board.Board;
import com.example.pedalboard.domain.board.BoardId;
import com.example.pedalboard.domain.board.BoardName;
import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalId;
import com.example.pedalboard.domain.pedal.PedalName;
import com.example.pedalboard.domain.pedal.PedalToCreate;
import com.example.pedalboard.domain.pedal.Placement;
import com.example.pedalboard.domain.user.UserId;
import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.SurfaceArea;
import com.example.pedalboard.domain.value.Coordinate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardRepositoryAdapter {

	private final BoardRepository boardRepository;
	private final PedalRepository pedalRepository;
	private final UserRepository userRepository;

	public BoardRepositoryAdapter(BoardRepository boardRepository, PedalRepository pedalRepository,
			UserRepository userRepository) {
		this.boardRepository = boardRepository;
		this.pedalRepository = pedalRepository;
		this.userRepository = userRepository;
	}

	public Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId) {
		UserModel user = userRepository.findById(userId.value())
				.orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));
		BoardModel saved = boardRepository
				.save(new BoardModel(name.value(), surfaceArea.width(), surfaceArea.height(), user));
		return toDomain(saved);
	}

	public Optional<Board> findByName(BoardName name) {
		return boardRepository.findByName(name.value()).map(BoardRepositoryAdapter::toDomain);
	}

	public List<Board> findByUserId(UserId userId) {
		return boardRepository.findByUserId(userId.value()).stream().map(BoardRepositoryAdapter::toDomain).toList();
	}

	public Optional<Board> findById(BoardId id) {
		return boardRepository.findById(id.value()).map(BoardRepositoryAdapter::toDomain);
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
		return Optional.of(toDomain(saved));
	}

	private static Board toDomain(BoardModel entity) {
		if (entity == null)
			return null;
		List<Pedal> domainPedals = entity.getPedals().stream().map(BoardRepositoryAdapter::toDomain).toList();
		BoardName boardName = new BoardName(entity.getName());
		if (entity.getUser() == null || entity.getUser().getId() == null) {
			throw new IllegalStateException("Board " + entity.getId() + " has no associated user");
		}
		UserId userId = new UserId(entity.getUser().getId());
		return new Board(new BoardId(entity.getId()), userId, boardName,
				new SurfaceArea(entity.getWidth(), entity.getHeight()), domainPedals);
	}

	private static Pedal toDomain(PedalModel entity) {
		if (entity == null)
			return null;
		Color color = new Color(entity.getColor());
		if (entity.getPlacement() == null || entity.getPlacement() <= 0) {
			throw new IllegalStateException(
					"Pedal " + entity.getId() + " has invalid placement: " + entity.getPlacement());
		}
		Placement placement = new Placement(entity.getPlacement());
		return new Pedal(new PedalId(entity.getId()), entity.getBoard() != null ? entity.getBoard().getId() : null,
				new PedalName(entity.getName()), new SurfaceArea(entity.getWidth(), entity.getHeight()), color,
				new Coordinate(entity.getX(), entity.getY()), placement);
	}
}
