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

        if (col == toCol) {
            // single move forward
            if (toRow == row + direction && board.getPieceAt(toRow, toCol) == null) {
                return true;
            }
            // double move from starting position
            if (row == startRow && toRow == row + 2 * direction &&
                    board.getPieceAt(row + direction, col) == null &&
                    board.getPieceAt(toRow, toCol) == null) {
                return true;
            }
            return false;
        }

        // capture move (diagonal)
        if (Math.abs(col - toCol) == 1 && toRow == row + direction) {
            Piece target = board.getPieceAt(toRow, toCol);

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

        if (col != toCol && board.getPieceAt(toRow, toCol) == null) {
            board.setPieceAt(row, toCol, null);
        }

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