package io.github.anderslunddev.pedalboard.service.pedal;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.model.CableRepositoryAdapter;
import io.github.anderslunddev.pedalboard.model.PedalRepositoryAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PedalService {

	private final PedalRepositoryAdapter pedalRepositoryAdapter;
	private final CableRepositoryAdapter cableRepositoryAdapter;

	public PedalService(PedalRepositoryAdapter pedalRepositoryAdapter, CableRepositoryAdapter cableRepositoryAdapter) {
		this.pedalRepositoryAdapter = pedalRepositoryAdapter;
		this.cableRepositoryAdapter = cableRepositoryAdapter;
	}

	@Transactional
	public Optional<Pedal> updatePedalPosition(PedalId id, Coordinate coordinate) {
		return pedalRepositoryAdapter.updatePosition(id, coordinate);
	}

	@Transactional
	public boolean deletePedal(PedalId id) {
		cableRepositoryAdapter.deleteBySourcePedalIdOrDestinationPedalId(id);
		return pedalRepositoryAdapter.deleteByIdIfExists(id);
	}
}
