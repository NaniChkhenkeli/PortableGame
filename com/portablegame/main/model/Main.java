package com.portablegame.main.model;

import com.portablegame.main.model.Board;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Board board = new Board();

        System.out.println("Initial Board:");
        board.printBoard();

        while (true) {
            System.out.println("Enter move (e.g., e2 e4) or 'quit' to exit:");
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("quit")) {
                break;
            }

            String[] move = input.split(" ");
            if (move.length != 2) {
                System.out.println("Invalid input! Please enter a valid move.");
                continue;
            }

            String start = move[0];
            String target = move[1];

            int startRow = 8 - Character.getNumericValue(start.charAt(1));  // Convert to board row (8 to 1)
            int startCol = start.charAt(0) - 'a'; // Convert 'a'-'h' to 0-7
            int targetRow = 8 - Character.getNumericValue(target.charAt(1));
            int targetCol = target.charAt(0) - 'a';

            if (board.movePiece(startRow, startCol, targetRow, targetCol)) {
                board.printBoard();
            } else {
                System.out.println("Invalid move! Try again.");
            }
        }

        scanner.close();
    }
}
