package com.portablegame.main.model;

public class Knight extends Piece {
    public Knight(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - this.row);
        int colDiff = Math.abs(toCol - this.col);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }


    @Override
    public String getSymbol() {
        return color.equals("white") ? "♘" : "♞";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "N" : "n";
    }
}