package io.github.anderslunddev.pedalboard.service.pedal;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.model.PedalRepositoryAdapter;
import io.github.anderslunddev.pedalboard.service.cable.CableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PedalService {

	private final PedalRepositoryAdapter pedalRepositoryAdapter;
	private final CableService cableService;

	public PedalService(PedalRepositoryAdapter pedalRepositoryAdapter, CableService cableService) {
		this.pedalRepositoryAdapter = pedalRepositoryAdapter;
		this.cableService = cableService;
	}

	@Transactional
	public Optional<Pedal> updatePedalPosition(PedalId id, Coordinate coordinate) {
		return pedalRepositoryAdapter.updatePosition(id, coordinate);
	}

	@Transactional
	public boolean deletePedal(PedalId id) {
		cableService.deleteCablesForPedal(id);
		return pedalRepositoryAdapter.deleteByIdIfExists(id);
	}
}
