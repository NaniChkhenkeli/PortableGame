package com.portablegame.main.model;


public abstract class Piece {
    protected String color;
    protected int row;
    protected int col;
    protected Board board;
    protected boolean hasMoved = false;
    public Piece(String color, int row, int col, Board board) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.board = board;
    }

    public abstract String getFENSymbol();
    public abstract boolean isValidMove(int toRow, int toCol);
    public abstract String getSymbol();

    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getColor() { return color; }
    public boolean hasMoved() { return hasMoved; }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void moveTo(int toRow, int toCol) {
        setPosition(toRow, toCol);
        hasMoved = true;
    }

    protected boolean isSameColor(Piece target) {
        return target != null && target.getColor().equals(color);
    }

    protected boolean isOpponent(Piece target) {
        return target != null && !target.getColor().equals(color);
    }
}