package io.github.anderslunddev.pedalboard.port;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;

import java.util.Optional;
import java.util.UUID;

/**
 * Outgoing port: load, update, and remove pedals; resolve board ownership for authorization.
 */
public interface PedalPersistencePort {

	Optional<Pedal> findById(PedalId id);

	Optional<Pedal> updatePosition(PedalId id, Coordinate coordinate);

	boolean deleteByIdIfExists(PedalId id);

	Optional<UUID> findBoardOwnerId(PedalId pedalId);
}
