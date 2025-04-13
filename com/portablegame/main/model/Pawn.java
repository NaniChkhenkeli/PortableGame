package com.portablegame.main.model;

public class Pawn extends Piece {
    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        int direction = color.equals("white") ? -1 : 1;
        int startRow = color.equals("white") ? 6 : 1;

        // Forward move (1 square)
        if (col == toCol && board.getPieceAt(toRow, toCol) == null) {
            if (toRow == row + direction) return true;

            // Double move from starting position
            if (row == startRow && toRow == row + 2 * direction &&
                    board.getPieceAt(row + direction, col) == null) {
                return true;
            }
        }

        // Capture (including en passant)
        if (Math.abs(col - toCol) == 1 && toRow == row + direction) {
            // Normal capture
            if (isOpponent(board.getPieceAt(toRow, toCol))) return true;

            // En passant
            String epTarget = board.getEnPassantTarget();
            if (epTarget != null &&
                    toRow == (color.equals("white") ? 2 : 5) &&
                    toCol == (epTarget.charAt(0) - 'a')) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♙" : "♟";
    }
}