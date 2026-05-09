package io.github.anderslunddev.pedalboard.service.pedal;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalName;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.port.PedalPersistencePort;
import io.github.anderslunddev.pedalboard.service.cable.CableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedalServiceTest {

	@Mock
	private PedalPersistencePort pedalPersistence;

	@Mock
	private CableService cableService;

	@InjectMocks
	private PedalService pedalService;

	@Test
	void updatePedalPositionDelegatesToPersistenceAndReturnsResult() {
		UUID rawId = UUID.randomUUID();
		PedalId id = new PedalId(rawId);
		Coordinate coordinate = new Coordinate(10.0, 20.0);

		Pedal pedal = new Pedal(id, new PedalName("Test"), new SurfaceArea(5.0, 10.0),
				new Color("#ffffff"), coordinate, new Placement(1));

		when(pedalPersistence.updatePosition(id, coordinate)).thenReturn(Optional.of(pedal));

		Optional<Pedal> result = pedalService.updatePedalPosition(id, coordinate);

		assertTrue(result.isPresent());
		assertEquals(pedal, result.get());
		verify(pedalPersistence).updatePosition(id, coordinate);
		verifyNoInteractions(cableService);
	}

	@Test
	void updatePedalPositionPropagatesEmptyWhenNotFound() {
		PedalId id = new PedalId(UUID.randomUUID());
		Coordinate coordinate = new Coordinate(1.0, 2.0);

		when(pedalPersistence.updatePosition(id, coordinate)).thenReturn(Optional.empty());

		Optional<Pedal> result = pedalService.updatePedalPosition(id, coordinate);

		assertTrue(result.isEmpty());
		verify(pedalPersistence).updatePosition(id, coordinate);
		verifyNoInteractions(cableService);
	}

	@Test
	void deletePedalDeletesCablesThenPedalAndReturnsTrueWhenDeleted() {
		PedalId id = new PedalId(UUID.randomUUID());

		when(pedalPersistence.deleteByIdIfExists(id)).thenReturn(true);

		boolean deleted = pedalService.deletePedal(id);

		assertTrue(deleted);
		verify(cableService).deleteCablesForPedal(id);
		verify(pedalPersistence).deleteByIdIfExists(id);
	}

	@Test
	void deletePedalDeletesCablesThenReturnsFalseWhenPedalMissing() {
		PedalId id = new PedalId(UUID.randomUUID());

		when(pedalPersistence.deleteByIdIfExists(id)).thenReturn(false);

		boolean deleted = pedalService.deletePedal(id);

		assertFalse(deleted);
		verify(cableService).deleteCablesForPedal(id);
		verify(pedalPersistence).deleteByIdIfExists(id);
	}
}
