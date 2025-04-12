package com.portablegame.main.model;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.printBoard();

        System.out.println("\nMoving white pawn from e2 to e4:");
        board.movePiece("e2", "e4", "white");
        board.printBoard();

        System.out.println("\nMoving white knight from g1 to f3:");
        board.movePiece("g1", "f3", "white");
        board.printBoard();

        PGNReader pgnReader = new PGNReader(board);
        System.out.println("\nReading moves from PGN file...");
        pgnReader.readPGN("path_to_your_pgn_file.pgn");

        board.printBoard();
    }
}
