package com.example.pedalboard.board;

import com.example.pedalboard.pedal.Pedal;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private Double width;

    private Double height;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedal> pedals = new ArrayList<>();

    public Board() {
    }

    public Board(String name, Double width, Double height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public List<Pedal> getPedals() {
        return pedals;
    }

    public void setPedals(List<Pedal> pedals) {
        this.pedals = pedals;
    }
}

