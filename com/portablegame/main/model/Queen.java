package com.portablegame.main.model;

public class Queen extends Piece {
    public Queen(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        boolean isStraight = targetRow == row || targetCol == col;
        boolean isDiagonal = Math.abs(targetRow - row) == Math.abs(targetCol - col);

        if (isStraight || isDiagonal) {
            if (isPathClear(targetRow, targetCol)) {
                Piece targetPiece = board.getPieceAt(targetRow, targetCol);
                return targetPiece == null || !targetPiece.color.equals(color);
            }
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♕" : "♛";
    }
}
