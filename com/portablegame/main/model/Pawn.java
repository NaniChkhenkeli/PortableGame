package com.portablegame.main.model;

public class Pawn extends Piece {
    public Pawn(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        int direction = color.equals("white") ? -1 : 1;
        int startRow = color.equals("white") ? 6 : 1;

        if (targetCol == col) {
            if (targetRow == row + direction) {
                return board.getPieceAt(targetRow, targetCol) == null;
            }
            else if (row == startRow && targetRow == row + 2 * direction) {
                return board.getPieceAt(targetRow, targetCol) == null &&
                        board.getPieceAt(row + direction, col) == null;
            }
        }
        else if (Math.abs(targetCol - col) == 1 && targetRow == row + direction) {
            Piece targetPiece = board.getPieceAt(targetRow, targetCol);
            return targetPiece != null && !targetPiece.color.equals(color);
        }

        return false;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♙" : "♟";
    }
}
