package io.github.anderslunddev.pedalboard.domain.pedal;

import io.github.anderslunddev.pedalboard.domain.value.Color;
import io.github.anderslunddev.pedalboard.domain.value.Coordinate;
import io.github.anderslunddev.pedalboard.domain.value.SurfaceArea;

import java.util.Objects;
import java.util.Optional;

public final class PedalToCreate {

	private final PedalName name;
	private final SurfaceArea surfaceArea;
	private final Color color;
	private final Coordinate coordinate;
	private final Placement placement; // null = let domain decide next placement

	public PedalToCreate(PedalName name, SurfaceArea surfaceArea, Color color, Coordinate coordinate,
			Placement placement) {
		this.name = Objects.requireNonNull(name, "Pedal name must not be null");
		this.surfaceArea = Objects.requireNonNull(surfaceArea, "Pedal surface area must not be null");
		this.color = Objects.requireNonNull(color, "Pedal color must not be null");
		this.coordinate = Objects.requireNonNull(coordinate, "Pedal coordinate must not be null");
		this.placement = placement; // Can be null - domain will decide placement
	}

	public PedalName getName() {
		return name;
	}

	public SurfaceArea getSurfaceArea() {
		return surfaceArea;
	}

	public Color getColor() {
		return color;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public Optional<Placement> getPlacement() {
		return Optional.ofNullable(placement);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private PedalName name;
		private SurfaceArea surfaceArea;
		private Color color;
		private Coordinate coordinate;
		private Placement placement;

		private Builder() {
		}

		public PedalToCreate build() {
			return new PedalToCreate(name, surfaceArea, color, coordinate, placement);
		}

		public Builder name(PedalName name) {
			this.name = name;
			return this;
		}

		public Builder surfaceArea(SurfaceArea surfaceArea) {
			this.surfaceArea = surfaceArea;
			return this;
		}

		public Builder color(Color color) {
			this.color = color;
			return this;
		}

		public Builder coordinate(Coordinate coordinate) {
			this.coordinate = coordinate;
			return this;
		}

		public Builder placement(Placement placement) {
			this.placement = placement;
			return this;
		}
	}
}
