package com.portablegame.main.model;

public class Knight extends Piece {
    public Knight(String color, int row, int col, Board board) {
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

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♘" : "♞";
    }
}
