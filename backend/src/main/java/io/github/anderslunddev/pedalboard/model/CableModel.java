package io.github.anderslunddev.pedalboard.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
class CableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	private UUID boardId;

	private UUID sourcePedalId;

	private UUID destinationPedalId;

	@ElementCollection
	@CollectionTable(name = "cable_path_point", joinColumns = @JoinColumn(name = "cable_id"))
	private List<PathPoint> pathPoints = new ArrayList<>();

	private Double totalLength;

	public CableModel() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getBoardId() {
		return boardId;
	}

	public void setBoardId(UUID boardId) {
		this.boardId = boardId;
	}

	public UUID getSourcePedalId() {
		return sourcePedalId;
	}

	public void setSourcePedalId(UUID sourcePedalId) {
		this.sourcePedalId = sourcePedalId;
	}

	public UUID getDestinationPedalId() {
		return destinationPedalId;
	}

	public void setDestinationPedalId(UUID destinationPedalId) {
		this.destinationPedalId = destinationPedalId;
	}

	public List<PathPoint> getPathPoints() {
		return pathPoints;
	}

	public void setPathPoints(List<PathPoint> pathPoints) {
		this.pathPoints = pathPoints;
	}

	public Double getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(Double totalLength) {
		this.totalLength = totalLength;
	}
}
