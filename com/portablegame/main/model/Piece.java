package com.portablegame.main.model;

public abstract class Piece {
    protected String color;
    protected int row;
    protected int col;
    protected Board board;

    public Piece(String color, int row, int col, Board board) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.board = board;
    }

    public abstract boolean isValidMove(int targetRow, int targetCol);
    public abstract String getSymbol();

    public String getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isWhite() {
        return "white".equals(color);
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Checks if all squares between the current position and the target position are empty.
     * This does NOT include the target square itself.
     */
    protected boolean isPathClear(int targetRow, int targetCol) {
        int rowStep = Integer.compare(targetRow, row);
        int colStep = Integer.compare(targetCol, col);

        int currentRow = row + rowStep;
        int currentCol = col + colStep;

        while (currentRow != targetRow || currentCol != targetCol) {
            if (board.getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }


}
