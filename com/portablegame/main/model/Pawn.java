package com.portablegame.main.model;

public class Pawn extends Piece {
    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        int direction = isWhite() ? -1 : 1;
        int startRow = isWhite() ? 6 : 1;

        // Forward move
        if (targetCol == col) {
            if (targetRow == row + direction) {
                return board.getPieceAt(targetRow, targetCol) == null;
            } else if (row == startRow && targetRow == row + 2 * direction) {
                return board.getPieceAt(row + direction, col) == null &&
                        board.getPieceAt(targetRow, targetCol) == null;
            }
        }
        // Capture move
        else if (Math.abs(targetCol - col) == 1 && targetRow == row + direction) {
            Piece targetPiece = board.getPieceAt(targetRow, targetCol);
            return targetPiece != null && !targetPiece.color.equals(color);
        }

        return false;
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♙" : "♟";
    }
}
