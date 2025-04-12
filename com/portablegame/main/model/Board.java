package com.portablegame.main.model;

public class Board {

    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("black", 1, i, this);
            board[6][i] = new Pawn("white", 6, i, this);
        }

        board[0][0] = new Rook("black", 0, 0, this);
        board[0][1] = new Knight("black", 0, 1, this);
        board[0][2] = new Bishop("black", 0, 2, this);
        board[0][3] = new Queen("black", 0, 3, this);
        board[0][4] = new King("black", 0, 4, this);
        board[0][5] = new Bishop("black", 0, 5, this);
        board[0][6] = new Knight("black", 0, 6, this);
        board[0][7] = new Rook("black", 0, 7, this);

        board[7][0] = new Rook("white", 7, 0, this);
        board[7][1] = new Knight("white", 7, 1, this);
        board[7][2] = new Bishop("white", 7, 2, this);
        board[7][3] = new Queen("white", 7, 3, this);
        board[7][4] = new King("white", 7, 4, this);
        board[7][5] = new Bishop("white", 7, 5, this);
        board[7][6] = new Knight("white", 7, 6, this);
        board[7][7] = new Rook("white", 7, 7, this);
    }

    public Piece getPieceAt(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }

    public void movePiece(String from, String to, String color) {
        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
        int fromCol = from.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));
        int toCol = to.charAt(0) - 'a';

        Piece pieceToMove = getPieceAt(fromRow, fromCol);

        if (pieceToMove == null || !pieceToMove.getColor().equals(color)) {
            System.out.println("Invalid piece or color.");
            return;
        }

        if (pieceToMove.isValidMove(toRow, toCol)) {
            board[toRow][toCol] = pieceToMove;
            board[fromRow][fromCol] = null;
            System.out.println("Moved " + pieceToMove.getClass().getSimpleName() + " from " + from + " to " + to);
        } else {
            System.out.println("Invalid move for " + pieceToMove.getClass().getSimpleName());
        }
    }

    public void printBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(row, col);
                System.out.print(piece == null ? "-- " : piece.getSymbol() + " ");
            }
            System.out.println();
        }
    }

    public void readPGN(String filePath) {
        PGNReader pgnReader = new PGNReader(this);
        pgnReader.readPGN(filePath);
    }
}
