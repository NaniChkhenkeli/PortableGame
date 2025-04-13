package com.portablegame.main.model;

// MoveCoordinates.java
public class MoveCoordinates {
    private final String from;
    private final String to;

    public MoveCoordinates(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public static MoveCoordinates fromAlgebraic(String notation, String color) {
        // Parse notation like e2e4 into from = "e2", to = "e4"
        if (notation.length() != 4) return null;
        return new MoveCoordinates(notation.substring(0, 2), notation.substring(2, 4));
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getToRow() {
        return 8 - Character.getNumericValue(to.charAt(1));
    }

    public int getToCol() {
        return to.charAt(0) - 'a';
    }
}

