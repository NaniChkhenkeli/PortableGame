package com.portablegame.main.model;

public class King extends Piece {
    public King(String color, int row, int col, Board board) {
        super(color, row, col, board);
    }

    @Override
    public boolean isValidMove(int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - row);
        int colDiff = Math.abs(toCol - col);

        // Normal king move (1 square)
        if ((rowDiff <= 1 && colDiff <= 1) && !(rowDiff == 0 && colDiff == 0)) {
            return !isOpponent(board.getPieceAt(toRow, toCol));
        }

        // Castling
        if (rowDiff == 0 && colDiff == 2 && row == (color.equals("white") ? 7 : 0)) {
            return isValidCastle(toCol);
        }
        return false;
    }

    private boolean isValidCastle(int toCol) {
        int rookCol = toCol > col ? 7 : 0;
        Piece rook = board.getPieceAt(row, rookCol);

        // Check if pieces are correct and haven't moved
        if (!(rook instanceof Rook) || isOpponent(rook)) return false;

        // Check if path is clear and not under attack
        int step = toCol > col ? 1 : -1;
        for (int c = col + step; c != rookCol; c += step) {
            if (board.getPieceAt(row, c) != null) return false;
            if (c != toCol && board.isSquareUnderAttack(row, c, color.equals("white") ? "black" : "white")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getSymbol() {
        return color.equals("white") ? "♔" : "♚";
    }
}