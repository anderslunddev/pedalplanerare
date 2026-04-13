package io.github.anderslunddev.pedalboard.service.pedal;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.port.PedalPersistencePort;
import io.github.anderslunddev.pedalboard.service.cable.CableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PedalService {

	private final PedalPersistencePort pedalPersistence;
	private final CableService cableService;

	public PedalService(PedalPersistencePort pedalPersistence, CableService cableService) {
		this.pedalPersistence = pedalPersistence;
		this.cableService = cableService;
	}

	@Transactional
	public Optional<Pedal> updatePedalPosition(PedalId id, Coordinate coordinate) {
		return pedalPersistence.updatePosition(id, coordinate);
	}

	@Transactional
	public boolean deletePedal(PedalId id) {
		cableService.deleteCablesForPedal(id);
		return pedalPersistence.deleteByIdIfExists(id);
	}
}
