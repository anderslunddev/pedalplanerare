package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.Board;
import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.board.BoardMother;
import io.github.anderslunddev.pedalboard.domain.board.BoardName;
import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardRepositoryAdapterTest {

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private PedalRepository pedalRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BoardModelConverter converter;

	@Mock
	private PedalModelConverter pedalConverter;

	@InjectMocks
	private BoardRepositoryAdapter adapter;

	@Test
	void createBoard_shouldLoadUserSaveBoardAndConvert() {
		UUID userUuid = UUID.randomUUID();
		UserId userId = new UserId(userUuid);
		BoardName boardName = new BoardName("My Board");
		SurfaceArea area = new SurfaceArea(60.0, 30.0);

		UserModel userModel = new UserModel();
		when(userRepository.findById(userUuid)).thenReturn(Optional.of(userModel));

		BoardModel toSave = new BoardModel();
		when(converter.toEntity(boardName, area, userModel)).thenReturn(toSave);

		BoardModel savedModel = new BoardModel();
		when(boardRepository.save(toSave)).thenReturn(savedModel);

		Board expectedBoard = BoardMother.withIdAndUser(new BoardId(UUID.randomUUID()), userId);
		when(converter.toDomain(savedModel)).thenReturn(expectedBoard);

		Board result = adapter.createBoard(boardName, area, userId);

		assertSame(expectedBoard, result);
		verify(userRepository).findById(userUuid);
		verify(converter).toEntity(boardName, area, userModel);
		verify(boardRepository).save(toSave);
		verify(converter).toDomain(savedModel);
	}

	@Test
	void createBoard_shouldThrowWhenUserNotFound() {
		UUID userUuid = UUID.randomUUID();
		UserId userId = new UserId(userUuid);
		when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> adapter.createBoard(new BoardName("X"), new SurfaceArea(10.0, 5.0), userId));

		verify(boardRepository, never()).save(any());
	}

	@Test
	void findByName_shouldDelegateToRepositoryAndConverter() {
		BoardName name = new BoardName("Existing");
		BoardModel model = new BoardModel();
		when(boardRepository.findByName(name.value())).thenReturn(Optional.of(model));
		Board expected = BoardMother.withName(name.value());
		when(converter.toDomain(model)).thenReturn(expected);

		Optional<Board> result = adapter.findByName(name);

		assertTrue(result.isPresent());
		assertSame(expected, result.get());
		verify(boardRepository).findByName(name.value());
		verify(converter).toDomain(model);
	}

	@Test
	void findByName_shouldReturnEmptyWhenMissing() {
		BoardName name = new BoardName("Missing");
		when(boardRepository.findByName(name.value())).thenReturn(Optional.empty());

		Optional<Board> result = adapter.findByName(name);

		assertTrue(result.isEmpty());
		verify(boardRepository).findByName(name.value());
		verifyNoInteractions(converter);
	}

	@Test
	void findByUserId_shouldMapAllBoardsViaConverter() {
		UUID userUuid = UUID.randomUUID();
		UserId userId = new UserId(userUuid);

		BoardModel m1 = new BoardModel();
		BoardModel m2 = new BoardModel();
		when(boardRepository.findByUserId(userUuid)).thenReturn(List.of(m1, m2));

		Board b1 = BoardMother.withName("B1");
		Board b2 = BoardMother.withName("B2");
		when(converter.toDomain(m1)).thenReturn(b1);
		when(converter.toDomain(m2)).thenReturn(b2);

		List<Board> result = adapter.findByUserId(userId);

		assertEquals(List.of(b1, b2), result);
		verify(boardRepository).findByUserId(userUuid);
		verify(converter).toDomain(m1);
		verify(converter).toDomain(m2);
	}

	@Test
	void findById_shouldReturnMappedBoardWhenPresent() {
		UUID id = UUID.randomUUID();
		BoardId boardId = new BoardId(id);
		BoardModel model = new BoardModel();
		when(boardRepository.findById(id)).thenReturn(Optional.of(model));

		Board expected = BoardMother.withIdAndUser(boardId, new UserId(UUID.randomUUID()));
		when(converter.toDomain(model)).thenReturn(expected);

		Optional<Board> result = adapter.findById(boardId);

		assertTrue(result.isPresent());
		assertSame(expected, result.get());
		verify(boardRepository).findById(id);
		verify(converter).toDomain(model);
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		UUID id = UUID.randomUUID();
		BoardId boardId = new BoardId(id);
		when(boardRepository.findById(id)).thenReturn(Optional.empty());

		Optional<Board> result = adapter.findById(boardId);

		assertTrue(result.isEmpty());
		verify(boardRepository).findById(id);
		verifyNoInteractions(converter);
	}

	@Test
	void deleteBoard_shouldDelegateToRepository() {
		UUID id = UUID.randomUUID();
		BoardId boardId = new BoardId(id);

		adapter.deleteBoard(boardId);

		verify(boardRepository).deleteById(id);
	}

	@Test
	void addPedalToBoard_shouldReturnEmptyWhenBoardMissing() {
		UUID id = UUID.randomUUID();
		BoardId boardId = new BoardId(id);
		when(boardRepository.findById(id)).thenReturn(Optional.empty());

		PedalToCreate pedalToCreate = PedalToCreate.builder()
				.name(new PedalName("Drive"))
				.surfaceArea(new SurfaceArea(10.0, 5.0))
				.color(new Color("#ffffff"))
				.coordinate(new Coordinate(1.0, 2.0))
				.build();

		Optional<Pedal> result = adapter.addPedalToBoard(boardId, pedalToCreate, new Placement(1));

		assertTrue(result.isEmpty());
		verify(boardRepository).findById(id);
		verifyNoInteractions(pedalRepository, converter, pedalConverter);
	}

	@Test
	void addPedalToBoard_shouldPersistAndConvertWhenBoardExists() {
		UUID id = UUID.randomUUID();
		BoardId boardId = new BoardId(id);
		BoardModel boardModel = new BoardModel();
		when(boardRepository.findById(id)).thenReturn(Optional.of(boardModel));

		PedalToCreate pedalToCreate = PedalToCreate.builder()
				.name(new PedalName("Drive"))
				.surfaceArea(new SurfaceArea(10.0, 5.0))
				.color(new Color("#ffffff"))
				.coordinate(new Coordinate(1.0, 2.0))
				.build();
		Placement placement = new Placement(1);

		PedalModel pedalToSave = new PedalModel();
		when(pedalConverter.toEntity(pedalToCreate, placement, boardModel)).thenReturn(pedalToSave);

		PedalModel savedPedalModel = new PedalModel();
		when(pedalRepository.save(pedalToSave)).thenReturn(savedPedalModel);

		Pedal expectedPedal = new Pedal(
				new PedalId(UUID.randomUUID()),
				boardId.value(),
				new PedalName("Drive"),
				new SurfaceArea(10.0, 5.0),
				new Color("#ffffff"),
				new Coordinate(1.0, 2.0),
				new Placement(1)
		);
		when(pedalConverter.toDomain(savedPedalModel)).thenReturn(expectedPedal);

		Optional<Pedal> result = adapter.addPedalToBoard(boardId, pedalToCreate, placement);

		assertTrue(result.isPresent());
		assertSame(expectedPedal, result.get());
		verify(boardRepository).findById(id);
		verify(pedalConverter).toEntity(pedalToCreate, placement, boardModel);
		verify(pedalRepository).save(pedalToSave);
		verify(pedalConverter).toDomain(savedPedalModel);
	}
}

