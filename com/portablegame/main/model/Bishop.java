package com.portablegame.main.model;

public class Bishop extends Piece {
    public Bishop(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        if (Math.abs(targetRow - row) == Math.abs(targetCol - col)) {
            int rowStep = targetRow > row ? 1 : -1;
            int colStep = targetCol > col ? 1 : -1;

            int r = row + rowStep;
            int c = col + colStep;

            while (r != targetRow && c != targetCol) {
                if (board.getPieceAt(r, c) != null) {
                    return false;
                }
                r += rowStep;
                c += colStep;
            }

            Piece targetPiece = board.getPieceAt(targetRow, targetCol);
            return targetPiece == null || !targetPiece.color.equals(color);
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♗" : "♝";
    }
}
