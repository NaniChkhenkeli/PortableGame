package com.portablegame.main.model;

public class Rook extends Piece {
    public Rook(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        if (targetRow == row || targetCol == col) {
            if (isPathClear(targetRow, targetCol)) {
                Piece targetPiece = board.getPieceAt(targetRow, targetCol);
                return targetPiece == null || !targetPiece.color.equals(color);
            }
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♖" : "♜";
    }
}
