package io.github.anderslunddev.pedalboard.model;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedalRepositoryAdapter {

	private final PedalRepository pedalRepository;
	private final PedalModelConverter converter;

	public PedalRepositoryAdapter(PedalRepository pedalRepository, PedalModelConverter converter) {
		this.pedalRepository = pedalRepository;
		this.converter = converter;
	}

	public Optional<Pedal> findById(PedalId id) {
		return pedalRepository.findById(id.value()).map(converter::toDomain);
	}

	public Optional<Pedal> updatePosition(PedalId id, Coordinate coordinate) {
		Optional<PedalModel> opt = pedalRepository.findById(id.value());
		if (opt.isEmpty()) {
			return Optional.empty();
		}
		PedalModel pedal = opt.get();
		pedal.setX(coordinate.x());
		pedal.setY(coordinate.y());
		PedalModel saved = pedalRepository.save(pedal);
		return Optional.of(converter.toDomain(saved));
	}

	public boolean deleteByIdIfExists(PedalId id) {
		UUID pedalId = id.value();
		if (!pedalRepository.existsById(pedalId)) {
			return false;
		}
		pedalRepository.deleteById(pedalId);
		return true;
	}

}
