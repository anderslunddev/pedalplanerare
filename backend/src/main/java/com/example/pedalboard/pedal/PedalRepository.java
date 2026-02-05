package com.example.pedalboard.pedal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedalRepository extends JpaRepository<Pedal, UUID> {
}

