package com.portablegame.main.model;

public class Board {
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        // Initialize the board with pieces in their initial positions
        setupBoard();
    }

    private void setupBoard() {
        // White pieces
        board[0][0] = new Rook("white", 0, 0, this);
        board[0][1] = new Knight("white", 0, 1, this);
        board[0][2] = new Bishop("white", 0, 2, this);
        board[0][3] = new Queen("white", 0, 3, this);
        board[0][4] = new King("white", 0, 4, this);
        board[0][5] = new Bishop("white", 0, 5, this);
        board[0][6] = new Knight("white", 0, 6, this);
        board[0][7] = new Rook("white", 0, 7, this);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("white", 1, i, this);
        }

        // Black pieces
        board[7][0] = new Rook("black", 7, 0, this);
        board[7][1] = new Knight("black", 7, 1, this);
        board[7][2] = new Bishop("black", 7, 2, this);
        board[7][3] = new Queen("black", 7, 3, this);
        board[7][4] = new King("black", 7, 4, this);
        board[7][5] = new Bishop("black", 7, 5, this);
        board[7][6] = new Knight("black", 7, 6, this);
        board[7][7] = new Rook("black", 7, 7, this);
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Pawn("black", 6, i, this);
        }
    }

    public Piece getPieceAt(int row, int col) {
        return board[row][col];
    }

    public boolean movePiece(int startRow, int startCol, int targetRow, int targetCol) {
        Piece piece = getPieceAt(startRow, startCol);
        if (piece == null) {
            System.out.println("No piece at starting position!");
            return false;
        }

        if (!piece.isValidMove(targetRow, targetCol)) {
            System.out.println("Invalid move for " + piece.getClass().getSimpleName());
            return false;
        }

        // Move the piece
        Piece targetPiece = getPieceAt(targetRow, targetCol);
        board[targetRow][targetCol] = piece;
        board[startRow][startCol] = null;
        piece.setPosition(targetRow, targetCol);
        return true;
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece == null) {
                    System.out.print(". ");
                } else {
                    System.out.print(piece.getSymbol() + " ");
                }
            }
            System.out.println(8 - row);
        }
        System.out.println("  a b c d e f g h");
    }
}
