package com.example.pedalboard.model;

import com.example.pedalboard.domain.pedal.Pedal;
import com.example.pedalboard.domain.pedal.PedalId;
import com.example.pedalboard.domain.pedal.PedalName;
import com.example.pedalboard.domain.pedal.Placement;
import com.example.pedalboard.domain.value.Color;
import com.example.pedalboard.domain.value.SurfaceArea;
import com.example.pedalboard.domain.value.Coordinate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedalRepositoryAdapter {

	private final PedalRepository pedalRepository;

	public PedalRepositoryAdapter(PedalRepository pedalRepository) {
		this.pedalRepository = pedalRepository;
	}

	public Optional<Pedal> findById(PedalId id) {
		return pedalRepository.findById(id.value()).map(PedalRepositoryAdapter::toDomain);
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
		return Optional.of(toDomain(saved));
	}

	public boolean deleteByIdIfExists(PedalId id) {
		UUID pedalId = id.value();
		if (!pedalRepository.existsById(pedalId)) {
			return false;
		}
		pedalRepository.deleteById(pedalId);
		return true;
	}

	private static Pedal toDomain(PedalModel entity) {
		if (entity == null)
			return null;
		Color color = new Color(entity.getColor());
		if (entity.getPlacement() == null || entity.getPlacement() <= 0) {
			throw new IllegalStateException(
					"Pedal " + entity.getId() + " has invalid placement: " + entity.getPlacement());
		}
		Placement placement = new Placement(entity.getPlacement());
		return new Pedal(new PedalId(entity.getId()), entity.getBoard() != null ? entity.getBoard().getId() : null,
				new PedalName(entity.getName()), new SurfaceArea(entity.getWidth(), entity.getHeight()), color,
				new Coordinate(entity.getX(), entity.getY()), placement);
	}
}
