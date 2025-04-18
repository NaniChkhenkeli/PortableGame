package com.portablegame.main.model;

public class Pawn extends Piece {
    private boolean hasMoved = false;

    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        if (row == toRow && col == toCol) {
            return false;
        }

        int direction = color.equals("white") ? -1 : 1;
        int startRow = color.equals("white") ? 6 : 1;

        // Normal move forward
        if (col == toCol) {
            // Single move forward
            if (toRow == row + direction && board.getPieceAt(toRow, toCol) == null) {
                return true;
            }
            // Double move from starting position
            if (row == startRow && toRow == row + 2 * direction &&
                    board.getPieceAt(row + direction, col) == null &&
                    board.getPieceAt(toRow, toCol) == null) {
                return true;
            }
            return false;
        }

        // Capture move (diagonal)
        if (Math.abs(col - toCol) == 1 && toRow == row + direction) {
            Piece target = board.getPieceAt(toRow, toCol);

            // Normal capture
            if (target != null && !target.getColor().equals(color)) {
                return true;
            }

            // En passant capture
            if (target == null) {
                String enPassantTarget = board.getEnPassantTarget();
                if (enPassantTarget != null) {
                    int epCol = enPassantTarget.charAt(0) - 'a';
                    int epRow = 8 - Character.getNumericValue(enPassantTarget.charAt(1));
                    if (toRow == epRow && toCol == epCol) {
                        // Check there's an opponent pawn in the adjacent file
                        Piece adjacentPawn = board.getPieceAt(row, toCol);
                        return adjacentPawn instanceof Pawn && !adjacentPawn.getColor().equals(color);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void moveTo(int toRow, int toCol) {
        int direction = color.equals("white") ? -1 : 1;

        // Handle en passant capture
        if (col != toCol && board.getPieceAt(toRow, toCol) == null) {
            // This is an en passant capture - remove the captured pawn
            board.setPieceAt(row, toCol, null);
        }

        // Set en passant target if pawn moves two squares
        if (Math.abs(toRow - row) == 2) {
            int epRow = row + direction;
            board.setEnPassantTarget("" + (char)('a' + toCol) + (8 - epRow));
        } else {
            board.setEnPassantTarget(null);
        }

        super.moveTo(toRow, toCol);
        hasMoved = true;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♙" : "♟";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "P" : "p";
    }
}