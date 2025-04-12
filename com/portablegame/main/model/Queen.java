package com.portablegame.main.model;

public class Queen extends Piece {
    public Queen(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        Rook rook = new Rook(color, row, col, board);
        Bishop bishop = new Bishop(color, row, col, board);

        return rook.isValidMove(targetRow, targetCol) || bishop.isValidMove(targetRow, targetCol);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♕" : "♛";
    }
}
