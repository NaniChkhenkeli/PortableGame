package com.portablegame.main.model;

public class King extends Piece {
    public King(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - row);
        int colDiff = Math.abs(targetCol - col);

        Piece targetPiece = board.getPieceAt(targetRow, targetCol);
        if (targetPiece != null && targetPiece.color.equals(color)) {
            return false;
        }

        return rowDiff <= 1 && colDiff <= 1;
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♔" : "♚";
    }
}
