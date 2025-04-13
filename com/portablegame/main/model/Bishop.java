package com.portablegame.main.model;

public class Bishop extends Piece {
    public Bishop(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - row);
        int colDiff = Math.abs(targetCol - col);
        if (rowDiff == colDiff) {
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
        return isWhite() ? "♗" : "♝";
    }
}
