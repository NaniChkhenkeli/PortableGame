package com.portablegame.main.model;

public class Rook extends Piece {
    public Rook(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        if (targetRow == row) {  // Horizontal move
            int step = targetCol > col ? 1 : -1;
            for (int c = col + step; c != targetCol; c += step) {
                if (board.getPieceAt(row, c) != null) {
                    return false;
                }
            }
        }
        else if (targetCol == col) {  // Vertical move
            int step = targetRow > row ? 1 : -1;
            for (int r = row + step; r != targetRow; r += step) {
                if (board.getPieceAt(r, col) != null) {
                    return false;
                }
            }
        }
        else {
            return false;
        }

        Piece targetPiece = board.getPieceAt(targetRow, targetCol);
        return targetPiece == null || !targetPiece.color.equals(color);
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♖" : "♜";
    }
}
