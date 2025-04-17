package com.portablegame.main.model;

public class Pawn extends Piece {
    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        // Can't move to same square
        if (row == toRow && col == toCol) {
            return false;
        }

        int direction = getDirection();
        int startRow = getStartRow();

        // Check basic movement patterns
        if (isVerticalMove(toCol)) {
            return validateVerticalMove(toRow, toCol, direction, startRow);
        } else if (isDiagonalCapture(toRow, toCol, direction)) {
            return validateCapture(toRow, toCol);
        }

        return false;
    }

    private int getDirection() {
        return color.equals("white") ? -1 : 1;
    }

    private int getStartRow() {
        return color.equals("white") ? 6 : 1;
    }

    private boolean isVerticalMove(int toCol) {
        return col == toCol;
    }

    private boolean validateVerticalMove(int toRow, int toCol, int direction, int startRow) {
        // Single square forward
        if (toRow == row + direction) {
            return board.getPieceAt(toRow, toCol) == null;
        }
        // Double move from starting position
        else if (row == startRow && toRow == row + 2 * direction) {
            return board.getPieceAt(toRow, toCol) == null &&
                    board.getPieceAt(row + direction, toCol) == null;
        }
        return false;
    }

    private boolean isDiagonalCapture(int toRow, int toCol, int direction) {
        return Math.abs(col - toCol) == 1 && toRow == row + direction;
    }

    private boolean validateCapture(int toRow, int toCol) {
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

        // Calculate correct rows for en passant
        int epTargetRow = color.equals("white") ? 3 : 4;
        int epVictimRow = color.equals("white") ? 3 : 4;
        int epCol = epTarget.charAt(0) - 'a';

        // Verify all en passant conditions
        return toRow == epTargetRow &&
                toCol == epCol &&
                board.getPieceAt(epVictimRow, toCol) instanceof Pawn &&
                isOpponent(board.getPieceAt(epVictimRow, toCol));
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