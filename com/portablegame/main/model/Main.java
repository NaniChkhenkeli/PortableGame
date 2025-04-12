package com.portablegame.main.model;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();

        System.out.println("Initial Chess Board Setup:\n");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + "  ");  // Print rank number
            for (int file = 0; file < 8; file++) {
                ChessModel.Square square = new ChessModel.Square(file, rank);
                ChessModel.Piece piece = board.getPiece(square);

                String output = piece == null ? "--" : piece.toString();
                System.out.print(output + " ");
            }
            System.out.println();
        }

        System.out.println("\n    a   b   c   d   e   f   g   h");
    }
}
