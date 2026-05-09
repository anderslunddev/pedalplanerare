package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalMother;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedalRepositoryAdapterTest {

	@Mock
	private PedalRepository pedalRepository;

	@Mock
	private PedalModelConverter converter;

	@InjectMocks
	private PedalRepositoryAdapter adapter;

	@Test
	void findById_shouldReturnMappedPedalWhenPresent() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		PedalModel model = new PedalModel();
		when(pedalRepository.findById(id)).thenReturn(Optional.of(model));

		Pedal expected = PedalMother.simple();
		when(converter.toDomain(model)).thenReturn(expected);

		Optional<Pedal> result = adapter.findById(pedalId);

		assertTrue(result.isPresent());
		assertSame(expected, result.get());
		verify(pedalRepository).findById(id);
		verify(converter).toDomain(model);
	}

	@Test
	void findById_shouldReturnEmptyWhenMissing() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		when(pedalRepository.findById(id)).thenReturn(Optional.empty());

		Optional<Pedal> result = adapter.findById(pedalId);

		assertTrue(result.isEmpty());
		verify(pedalRepository).findById(id);
		verifyNoInteractions(converter);
	}

	@Test
	void updatePosition_shouldReturnPedalWhenExists() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		Coordinate newCoordinate = new Coordinate(5.0, 6.0);

		PedalModel existingModel = new PedalModel();
		when(pedalRepository.findById(id)).thenReturn(Optional.of(existingModel));
		PedalModel savedModel = new PedalModel();
		when(pedalRepository.save(existingModel)).thenReturn(savedModel);

		Pedal expectedPedal = PedalMother.simple();
		when(converter.toDomain(savedModel)).thenReturn(expectedPedal);

		Optional<Pedal> result = adapter.updatePosition(pedalId, newCoordinate);

		assertTrue(result.isPresent());
		assertSame(expectedPedal, result.get());
		verify(pedalRepository).findById(id);
		verify(pedalRepository).save(existingModel);
		assertEquals(5.0, existingModel.getX());
		assertEquals(6.0, existingModel.getY());
		verify(converter).toDomain(savedModel);
	}

	@Test
	void updatePosition_shouldReturnEmptyWhenPedalMissing() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		Coordinate coordinate = new Coordinate(1.0, 2.0);
		when(pedalRepository.findById(id)).thenReturn(Optional.empty());

		Optional<Pedal> result = adapter.updatePosition(pedalId, coordinate);

		assertTrue(result.isEmpty());
		verify(pedalRepository).findById(id);
		verify(pedalRepository, never()).save(any());
		verifyNoInteractions(converter);
	}

	@Test
	void deleteByIdIfExists_shouldReturnTrueAndDeleteWhenExists() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		when(pedalRepository.existsById(id)).thenReturn(true);

		boolean result = adapter.deleteByIdIfExists(pedalId);

		assertTrue(result);
		verify(pedalRepository).existsById(id);
		verify(pedalRepository).deleteById(id);
	}

	@Test
	void deleteByIdIfExists_shouldReturnFalseWhenPedalMissing() {
		UUID id = UUID.randomUUID();
		PedalId pedalId = new PedalId(id);
		when(pedalRepository.existsById(id)).thenReturn(false);

		boolean result = adapter.deleteByIdIfExists(pedalId);

		assertFalse(result);
		verify(pedalRepository).existsById(id);
		verify(pedalRepository, never()).deleteById(any());
	}
}
