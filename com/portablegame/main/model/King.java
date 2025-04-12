package com.portablegame.main.model;

public class King extends Piece {
    public King(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - row);
        int colDiff = Math.abs(targetCol - col);

        return (rowDiff <= 1 && colDiff <= 1);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♔" : "♚";
    }
}
