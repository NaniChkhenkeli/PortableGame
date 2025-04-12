package com.portablegame.main.model;

public abstract class Piece {
    protected String color;
    protected int row;
    protected int col;
    protected Board board;

    public Piece(String color, int row, int col, Board board) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.board = board;
    }

    public String getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public abstract boolean isValidMove(int targetRow, int targetCol);

    public abstract String getSymbol();
}
