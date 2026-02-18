package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BoardRepositoryAdapter {

	private final BoardRepository boardRepository;
	private final PedalRepository pedalRepository;
	private final UserRepository userRepository;
	private final BoardModelConverter converter;
	private final PedalModelConverter pedalConverter;

	public BoardRepositoryAdapter(BoardRepository boardRepository, PedalRepository pedalRepository,
                                  UserRepository userRepository, BoardModelConverter converter, PedalModelConverter pedalConverter) {
		this.boardRepository = boardRepository;
		this.pedalRepository = pedalRepository;
		this.userRepository = userRepository;
        this.converter = converter;
        this.pedalConverter = pedalConverter;
    }

	public Board createBoard(BoardName name, SurfaceArea surfaceArea, UserId userId) {
		UserModel user = userRepository.findById(userId.value())
				.orElseThrow(() -> new IllegalArgumentException("User not found for id " + userId));
		BoardModel saved = boardRepository
				.save(converter.toEntity(name, surfaceArea, user));
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

		PedalModel pedal = pedalConverter.toEntity(pedalToCreate, placement, board);

		PedalModel saved = pedalRepository.save(pedal);
		return Optional.of(pedalConverter.toDomain(saved));
	}




}
