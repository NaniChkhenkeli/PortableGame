package com.portablegame.main.model;

public class Pawn extends Piece {
    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        // Can't move to the same position
        if (row == toRow && col == toCol) {
            return false;
        }

        int direction = color.equals("white") ? -1 : 1;
        int startRow = color.equals("white") ? 6 : 1;
        int promotionRow = color.equals("white") ? 0 : 7;

        // 1. Validate basic movement rules
        if (!isValidPawnMovement(toRow, toCol, direction, startRow)) {
            return false;
        }

        // 2. Check for blocking pieces
        if (isPathBlocked(toRow, toCol, direction)) {
            return false;
        }

        // 3. Validate captures (including en passant)
        if (Math.abs(col - toCol) == 1) {
            return isValidCapture(toRow, toCol, direction, promotionRow);
        }

        // 4. Check promotion
        if (toRow == promotionRow) {
            // Promotion will be handled by Board during move execution
            return true;
        }

        return true;
    }

    private boolean isValidPawnMovement(int toRow, int toCol, int direction, int startRow) {
        // Vertical movement
        if (col == toCol) {
            // Single move forward
            if (toRow == row + direction) {
                return board.getPieceAt(toRow, toCol) == null;
            }
            // Double move from starting position
            if (row == startRow && toRow == row + 2 * direction) {
                return board.getPieceAt(toRow, toCol) == null &&
                        board.getPieceAt(row + direction, toCol) == null;
            }
            return false;
        }
        return true;
    }

    private boolean isPathBlocked(int toRow, int toCol, int direction) {
        // For vertical moves, check if path is blocked
        if (col == toCol && Math.abs(toRow - row) > 1) {
            return board.getPieceAt(row + direction, toCol) != null;
        }
        return false;
    }

    private boolean isValidCapture(int toRow, int toCol, int direction, int promotionRow) {
        // Must move diagonally forward
        if (toRow != row + direction) {
            return false;
        }

        // Normal capture
        Piece target = board.getPieceAt(toRow, toCol);
        if (target != null && isOpponent(target)) {
            return true;
        }

        // En passant capture
        return isValidEnPassant(toRow, toCol);
    }

    private boolean isValidEnPassant(int toRow, int toCol) {
        String epTarget = board.getEnPassantTarget();
        if (epTarget == null) {
            return false;
        }

        int epRow = color.equals("white") ? 2 : 5;
        int epCol = epTarget.charAt(0) - 'a';

        return toRow == epRow &&
                toCol == epCol &&
                Math.abs(col - toCol) == 1;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♙" : "♟";  // Keep your current symbols
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "P" : "p";  // FEN uses P/p
    }
}