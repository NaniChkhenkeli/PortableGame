package com.portablegame.main.model;

public class Queen extends Piece {
    public Queen(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);

        if (!(rowDiff == 0 || colDiff == 0 || rowDiff == colDiff)) return false;
        if (!board.isPathClear(row, col, toRow, toCol)) return false;
        Piece target = board.getPieceAt(toRow, toCol);
        return target == null || isOpponent(target);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♕" : "♛";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "Q" : "q";
    }
}
