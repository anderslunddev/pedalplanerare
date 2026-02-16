package com.example.pedalboard.service.pedal;

import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalId;
import com.example.pedalboard.domain.value.Coordinate;
import com.example.pedalboard.model.CableRepositoryAdapter;
import com.example.pedalboard.model.PedalRepositoryAdapter;
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
