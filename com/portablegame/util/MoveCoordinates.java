package com.portablegame.util;

import com.portablegame.main.model.*;
import java.util.regex.*;

public class MoveCoordinates {
    public String from;
    public String to;
    public int fromRow;
    public int fromCol;
    public int toRow;
    public int toCol;

    public MoveCoordinates() {}

    public MoveCoordinates(String from, String to) {
        this.from = from;
        this.to = to;
        if (from != null && from.length() == 2) {
            this.fromCol = from.charAt(0) - 'a';
            this.fromRow = 8 - Character.getNumericValue(from.charAt(1));
        }
        if (to != null && to.length() == 2) {
            this.toCol = to.charAt(0) - 'a';
            this.toRow = 8 - Character.getNumericValue(to.charAt(1));
        }
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }

    @Override
    public String toString() {
        return from + "->" + to;
    }
}