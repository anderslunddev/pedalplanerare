package io.github.anderslunddev.pedalboard.domain.board;

import io.github.anderslunddev.pedalboard.domain.pedal.Pedal;
import io.github.anderslunddev.pedalboard.domain.pedal.PedalToCreate;
import io.github.anderslunddev.pedalboard.domain.pedal.Placement;
import io.github.anderslunddev.pedalboard.domain.user.UserId;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Board(BoardId id, UserId userId, BoardName name, SurfaceArea surfaceArea, List<Pedal> pedals) {

	public Board(BoardId id, UserId userId, BoardName name, SurfaceArea surfaceArea, List<Pedal> pedals) {
		this.id = Objects.requireNonNull(id, "Board id must not be null");
		this.userId = Objects.requireNonNull(userId, "Board userId must not be null");
		this.name = Objects.requireNonNull(name, "Board name must not be null");
		this.surfaceArea = Objects.requireNonNull(surfaceArea, "Board surface area must not be null");
		this.pedals = pedals != null ? List.copyOf(pedals) : List.of();
	}

	/**
	 * Resolves the placement for a new pedal on this board.
	 * <p>
	 * If the pedal has an explicit placement request, validates it's not already
	 * taken. If no placement is requested, assigns the next available placement
	 * number.
	 *
	 * @param pedalToCreate
	 *            the pedal that is about to be created
	 * @return the resolved placement (always &gt; 0)
	 * @throws IllegalArgumentException
	 *             if the requested placement is already taken
	 */
	public Placement resolvePlacementFor(PedalToCreate pedalToCreate) {
		Objects.requireNonNull(pedalToCreate, "pedalToCreate must not be null");
		int maxExisting = pedals.stream().map(Pedal::placement).mapToInt(Placement::value).max().orElse(0);

		Optional<Placement> placementOptional = pedalToCreate.getPlacement();

		if (placementOptional.isEmpty()) {
			// No explicit placement requested – append at the end.
			return new Placement(maxExisting + 1);
		}

		Placement requestedPlacement = placementOptional.get();
		boolean taken = pedals.stream().anyMatch(p -> p.placement().value() == requestedPlacement.value());
		if (taken) {
			throw new IllegalArgumentException(
					"Placement number " + requestedPlacement.value() + " is already in use.");
		}

		return requestedPlacement;
	}

	/**
	 * Returns true if the new pedal's rectangle would overlap any existing pedal on
	 * this board (same rule as drag: pedals must not overlap).
	 */
	public boolean wouldOverlapWithExisting(PedalToCreate toCreate) {
		Objects.requireNonNull(toCreate, "toCreate must not be null");
		return pedals.stream().anyMatch(existing -> existing.overlaps(toCreate));
	}
}
