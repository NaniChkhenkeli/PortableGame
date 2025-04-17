package com.portablegame.main.model;

/**
 * Represents the King piece and handles its movement logic,
 * including normal moves and castling.
 */
public class King extends Piece {
    private boolean hasMoved = false;

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

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

        // Normal king move
        if (rowDiff <= 1 && colDiff <= 1) {
            Piece target = board.getPieceAt(toRow, toCol);
            return target == null || isOpponent(target);
        }

        // Castling move
        if (row == toRow && colDiff == 2) {
            return board.isValidCastling(color, row, col, toCol);
        }

        return false;
    }


    @Override
    public void moveTo(int toRow, int toCol) {
        int colDiff = Math.abs(toCol - col);

        // Handle castling move
        if (colDiff == 2 && row == toRow) {
            int rookFromCol = (toCol > col) ? 7 : 0; // King-side or Queen-side
            int rookToCol = (toCol > col) ? toCol - 1 : toCol + 1;

            Piece rook = board.getPieceAt(row, rookFromCol);
            if (rook instanceof Rook) {
                board.setPieceAt(row, rookToCol, rook);
                board.setPieceAt(row, rookFromCol, null);
                ((Rook) rook).setHasMoved(true);
                rook.setPosition(row, rookToCol);
            }
        }

        // Standard move
        super.moveTo(toRow, toCol);
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
