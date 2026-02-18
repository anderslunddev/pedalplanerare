package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.board.BoardId;
import io.github.anderslunddev.pedalboard.domain.cable.Cable;
import io.github.anderslunddev.pedalboard.domain.cable.CableMother;
import io.github.anderslunddev.pedalboard.domain.cable.Length;
import io.github.anderslunddev.pedalboard.domain.cable.PathPoint;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CableRepositoryAdapterTest {

	@Mock
	private CableRepository cableRepository;

	@Mock
	private CableModelConverter converter;

	@InjectMocks
	private CableRepositoryAdapter adapter;

	@Test
	void saveCable_shouldConvertSaveAndReturnDomainCable() {
		BoardId boardId = new BoardId(UUID.randomUUID());
		PedalId sourcePedalId = new PedalId(UUID.randomUUID());
		PedalId destinationPedalId = new PedalId(UUID.randomUUID());
		List<PathPoint> pathPoints = List.of(new PathPoint(0.0, 0.0), new PathPoint(10.0, 10.0));
		Length totalLength = new Length(14.14);

		CableModel toSave = new CableModel();
		when(converter.toEntity(boardId, sourcePedalId, destinationPedalId, pathPoints, totalLength)).thenReturn(toSave);

		CableModel savedModel = new CableModel();
		when(cableRepository.save(toSave)).thenReturn(savedModel);

		Cable expected = CableMother.withBoardId(boardId);
		when(converter.toDomain(savedModel)).thenReturn(expected);

		Cable result = adapter.saveCable(boardId, sourcePedalId, destinationPedalId, pathPoints, totalLength);

		assertSame(expected, result);
		verify(converter).toEntity(boardId, sourcePedalId, destinationPedalId, pathPoints, totalLength);
		verify(cableRepository).save(toSave);
		verify(converter).toDomain(savedModel);
	}

	@Test
	void findByBoardId_shouldMapAllCablesViaConverter() {
		BoardId boardId = new BoardId(UUID.randomUUID());
		CableModel m1 = new CableModel();
		CableModel m2 = new CableModel();
		when(cableRepository.findByBoardId(boardId.value())).thenReturn(List.of(m1, m2));

		Cable c1 = CableMother.withBoardId(boardId);
		Cable c2 = CableMother.withBoardId(boardId);
		when(converter.toDomain(m1)).thenReturn(c1);
		when(converter.toDomain(m2)).thenReturn(c2);

		List<Cable> result = adapter.findByBoardId(boardId);

		assertEquals(List.of(c1, c2), result);
		verify(cableRepository).findByBoardId(boardId.value());
		verify(converter).toDomain(m1);
		verify(converter).toDomain(m2);
	}

	@Test
	void findByBoardId_shouldReturnEmptyListWhenNoCables() {
		BoardId boardId = new BoardId(UUID.randomUUID());
		when(cableRepository.findByBoardId(boardId.value())).thenReturn(List.of());

		List<Cable> result = adapter.findByBoardId(boardId);

		assertTrue(result.isEmpty());
		verify(cableRepository).findByBoardId(boardId.value());
		verifyNoInteractions(converter);
	}

	@Test
	void deleteByBoardId_shouldDeleteAllCablesForBoard() {
		BoardId boardId = new BoardId(UUID.randomUUID());
		CableModel cable1 = new CableModel();
		CableModel cable2 = new CableModel();
		when(cableRepository.findByBoardId(boardId.value())).thenReturn(List.of(cable1, cable2));

		adapter.deleteByBoardId(boardId);

		verify(cableRepository).findByBoardId(boardId.value());
		verify(cableRepository).deleteAll(List.of(cable1, cable2));
	}

	@Test
	void deleteBySourcePedalIdOrDestinationPedalId_shouldDeleteMatchingCables() {
		PedalId pedalId = new PedalId(UUID.randomUUID());
		CableModel cable1 = new CableModel();
		when(cableRepository.findBySourcePedalIdOrDestinationPedalId(pedalId.value())).thenReturn(List.of(cable1));

		adapter.deleteBySourcePedalIdOrDestinationPedalId(pedalId);

		verify(cableRepository).findBySourcePedalIdOrDestinationPedalId(pedalId.value());
		verify(cableRepository).deleteAll(List.of(cable1));
	}

	@Test
	void flush_shouldDelegateToRepository() {
		adapter.flush();
		verify(cableRepository).flush();
	}
}
