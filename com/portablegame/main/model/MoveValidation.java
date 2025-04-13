package com.portablegame.main.model;

// MoveValidation.java
import java.util.ArrayList;
import java.util.List;

public class MoveValidation {
    private final int moveNumber;
    private final String originalNotation;
    private final String color;
    private MoveCoordinates coordinates;
    private boolean isValid;
    private final List<String> errors = new ArrayList<>();

    public MoveValidation(int moveNumber, String notation, String color) {
        this.moveNumber = moveNumber;
        this.originalNotation = notation;
        this.color = color;
    }

    public void addError(String error) {
        errors.add("Move " + moveNumber + " (" + color + "): " + error);
        isValid = false;
    }

    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setCoordinates(MoveCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public MoveCoordinates getCoordinates() {
        return coordinates;
    }

    public List<String> getErrors() {
        return errors;
    }
}
