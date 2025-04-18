package com.portablegame.main.model;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        if (row == toRow && col == toCol) {
            return false;
        }

        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);

        // 1 square in any direction
        if (rowDiff <= 1 && colDiff <= 1) {
            Piece target = board.getPieceAt(toRow, toCol);
            return target == null || !target.getColor().equals(color);
        }

        // 2 squares horizontally
        if (row == toRow && colDiff == 2 && !hasMoved) {
            return board.isValidCastling(color, row, col, toCol);
        }

        return false;
    }

    @Override
    public void moveTo(int toRow, int toCol) {
        // Handle castling
        if (Math.abs(col - toCol) == 2) {
            // Determine rook positions
            int rookFromCol = toCol > col ? 7 : 0;
            int rookToCol = toCol > col ? 5 : 3;

            // Move the rook
            Piece rook = board.getPieceAt(row, rookFromCol);
            board.setPieceAt(row, rookToCol, rook);
            board.setPieceAt(row, rookFromCol, null);
            rook.setPosition(row, rookToCol);
        }

        super.moveTo(toRow, toCol);
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♔" : "♚";
    }

    @Override
    public String getFENSymbol() {
        return color.equals("white") ? "K" : "k";
    }
}