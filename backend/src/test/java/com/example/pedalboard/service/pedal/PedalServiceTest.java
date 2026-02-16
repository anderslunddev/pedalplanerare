package com.example.pedalboard.service.pedal;

import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalId;
import com.example.pedalboard.domain.pedal.PedalName;
import com.example.pedalboard.domain.pedal.Placement;
import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.SurfaceArea;
import com.example.pedalboard.domain.value.Coordinate;
import com.example.pedalboard.model.CableRepositoryAdapter;
import com.example.pedalboard.model.PedalRepositoryAdapter;
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
	private PedalRepositoryAdapter pedalRepositoryAdapter;

	@Mock
	private CableRepositoryAdapter cableRepositoryAdapter;

	@InjectMocks
	private PedalService pedalService;

	@Test
	void updatePedalPositionDelegatesToRepositoryAdapterAndReturnsResult() {
		UUID rawId = UUID.randomUUID();
		PedalId id = new PedalId(rawId);
		Coordinate coordinate = new Coordinate(10.0, 20.0);

		Pedal pedal = new Pedal(id, UUID.randomUUID(), new PedalName("Test"), new SurfaceArea(5.0, 10.0),
				new Color("#ffffff"), coordinate, new Placement(1));

		when(pedalRepositoryAdapter.updatePosition(id, coordinate)).thenReturn(Optional.of(pedal));

		Optional<Pedal> result = pedalService.updatePedalPosition(id, coordinate);

		assertTrue(result.isPresent());
		assertEquals(pedal, result.get());
		verify(pedalRepositoryAdapter).updatePosition(id, coordinate);
		verifyNoInteractions(cableRepositoryAdapter);
	}

	@Test
	void updatePedalPositionPropagatesEmptyWhenNotFound() {
		PedalId id = new PedalId(UUID.randomUUID());
		Coordinate coordinate = new Coordinate(1.0, 2.0);

		when(pedalRepositoryAdapter.updatePosition(id, coordinate)).thenReturn(Optional.empty());

		Optional<Pedal> result = pedalService.updatePedalPosition(id, coordinate);

		assertTrue(result.isEmpty());
		verify(pedalRepositoryAdapter).updatePosition(id, coordinate);
		verifyNoInteractions(cableRepositoryAdapter);
	}

	@Test
	void deletePedalDeletesCablesThenPedalAndReturnsTrueWhenDeleted() {
		PedalId id = new PedalId(UUID.randomUUID());

		when(pedalRepositoryAdapter.deleteByIdIfExists(id)).thenReturn(true);

		boolean deleted = pedalService.deletePedal(id);

		assertTrue(deleted);
		verify(cableRepositoryAdapter).deleteBySourcePedalIdOrDestinationPedalId(id);
		verify(pedalRepositoryAdapter).deleteByIdIfExists(id);
	}

	@Test
	void deletePedalDeletesCablesThenReturnsFalseWhenPedalMissing() {
		PedalId id = new PedalId(UUID.randomUUID());

		when(pedalRepositoryAdapter.deleteByIdIfExists(id)).thenReturn(false);

		boolean deleted = pedalService.deletePedal(id);

		assertFalse(deleted);
		verify(cableRepositoryAdapter).deleteBySourcePedalIdOrDestinationPedalId(id);
		verify(pedalRepositoryAdapter).deleteByIdIfExists(id);
	}
}
