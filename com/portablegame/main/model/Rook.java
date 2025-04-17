package com.portablegame.main.model;

public class Rook extends Piece {
    private boolean hasMoved = false;

    public Rook(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        // Must move in straight line
        if (row != toRow && col != toCol) {
            return false;
        }

        // Check path is clear
        if (!board.isPathClear(row, col, toRow, toCol)) {
            return false;
        }

        // Check target square
        Piece target = board.getPieceAt(toRow, toCol);
        return target == null || isOpponent(target);
    }

    public boolean hasMoved() {
        return hasMoved;
    }


    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♖" : "♜";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "R" : "r";
    }
}
