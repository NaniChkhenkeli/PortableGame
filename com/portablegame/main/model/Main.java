package com.portablegame.main.model;

import com.portablegame.util.PgnGameValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Chess Validator - Choose Mode:");
        System.out.println("1. Interactive Board Play");
        System.out.println("2. Validate PGN File");
        System.out.print("Select (1/2): ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                startInteractiveMode();
                break;
            case "2":
                startPgnValidationMode();
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    private static void startInteractiveMode() {
        Board board = new Board();
        System.out.println("\nInteractive Chess Board - Enter moves as 'e2 e4' or 'quit'");

        while (true) {
            board.printBoard();
            System.out.println("Current turn: " + board.getCurrentPlayer());

            System.out.print("Enter move: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("quit")) break;
            if (input.equals("reset")) {
                board.initializeBoard();
                continue;
            }

            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("Invalid format! Use 'from to' (e.g., 'e2 e4')");
                continue;
            }

            String from = parts[0];
            String to = parts[1];
            String promotion = null;

            // Handle pawn promotion
            if (to.length() > 2 && to.contains("=")) {
                promotion = to.substring(to.indexOf("=") + 1);
                to = to.substring(0, to.indexOf("="));
            }

            if (board.tryMove(from, to, promotion)) {
                // Check game status after move
                if (board.isCheckmate("white")) {
                    System.out.println("Checkmate! Black wins");
                    break;
                } else if (board.isCheckmate("black")) {
                    System.out.println("Checkmate! White wins");
                    break;
                } else if (board.isKingInCheck(board.getCurrentPlayer())) {
                    System.out.println("Check!");
                }
            } else {
                System.out.println("Invalid move! Try again.");
            }
        }
    }

    private static void startPgnValidationMode() {
        System.out.print("\nEnter PGN file path: ");
        String filePath = scanner.nextLine().trim();

        try {
            if (!Files.exists(Path.of(filePath))) {
                System.out.println("File not found!");
                return;
            }

            PgnGameValidator validator = new PgnGameValidator();
            validator.validatePgnFile(filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public static void demoChessRules(Board board) {
        // Demonstrate special moves
        System.out.println("\nDemonstrating Chess Rules:");

        // 1. En passant
        System.out.println("\nEn Passant Example:");
        board.tryMove("e2", "e4", null);
        board.tryMove("e7", "e5", null);
        board.tryMove("e4", "e5", null);  // White pawn advances
        board.tryMove("d7", "d5", null);  // Black pawn moves two squares
        board.printBoard();
        System.out.println("En passant available: " + board.getEnPassantTarget());
        board.tryMove("e5", "d6", null);  // White captures en passant
        board.printBoard();

        // 2. Castling
        System.out.println("\nCastling Example:");
        board.initializeBoard();
        board.tryMove("e2", "e4", null);
        board.tryMove("e7", "e5", null);
        board.tryMove("f1", "c4", null);
        board.tryMove("f8", "c5", null);
        board.tryMove("g1", "f3", null);
        board.tryMove("g8", "f6", null);
        board.printBoard();
        board.tryMove("e1", "g1", null);  // White kingside castling
        board.printBoard();

        // 3. Promotion
        System.out.println("\nPromotion Example:");
        board.initializeBoard();
        board.tryMove("e2", "e4", null);
        board.tryMove("d7", "d5", null);
        board.tryMove("e4", "d5", null);
        board.tryMove("e7", "e5", null);
        board.tryMove("d5", "d6", null);
        board.tryMove("e5", "e4", null);
        board.tryMove("d6", "d7", null);
        board.tryMove("e4", "e3", null);
        board.tryMove("d7", "d8=Q", null);  // Promotion to queen
        board.printBoard();
    }
}