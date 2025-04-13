package com.portablegame.main.model;

public class Bishop extends Piece {
    public Bishop(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);

        // Must move diagonally
        if (rowDiff != colDiff) return false;

        // Check path is clear
        if (!board.isPathClear(row, col, toRow, toCol)) return false;

        // Check target square
        Piece target = board.getPieceAt(toRow, toCol);
        return target == null || isOpponent(target);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♗" : "♝";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "B" : "b";
    }
}