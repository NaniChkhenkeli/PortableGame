package com.portablegame.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PgnParser {

    public static void main(String[] args) throws IOException {
        String pgnPath = "com/portablegame/util/PgnParser.java"; // Corrected path if the folder is named 'resources'

        List<String> games = parseGamesFromFile(pgnPath);

        for (String game : games) {
            System.out.println("=== New Game ===");
            List<String> moves = extractMoves(game);

            Board board = new Board();
            board.printBoard();

            boolean whiteToMove = true;

            for (String move : moves) {
                System.out.println((whiteToMove ? "White" : "Black") + " plays: " + move);
                // Placeholder: apply move to board
                // boolean isValid = MoveValidator.isValidMove(board, move, whiteToMove);
                // board.applyMove(move, whiteToMove);

                whiteToMove = !whiteToMove;
            }
        }
    }

    public static List<String> parseGamesFromFile(String path) throws IOException {
        String content = Files.readString(Path.of(path));
        String[] games = content.split("(?=\\[Event )");
        return Arrays.asList(games);
    }

    public static List<String> extractMoves(String gameText) {
        String[] lines = gameText.split("\n");
        StringBuilder moves = new StringBuilder();
        for (String line : lines) {
            if (!line.startsWith("[")) {
                moves.append(line).append(" ");
            }
        }
        // Remove result and move numbers
        String cleaned = moves.toString()
                .replaceAll("\\d+\\.", "")
                .replaceAll("\\{[^}]*\\}", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] moveArray = cleaned.split(" ");
        List<String> moveList = new ArrayList<>();

        for (String move : moveArray) {
            if (move.matches("1-0|0-1|1/2-1/2")) continue;
            moveList.add(move);
        }

        return moveList;
    }

    // ---------------- Board Class ----------------
    static class Board {
        private final String[][] board;

        public Board() {
            board = new String[8][8];
            setupInitialPosition();
        }

        private void setupInitialPosition() {
            String[] backRank = {"RO", "KN", "BI", "QU", "KI", "BI", "KN", "RO"};
            for (int i = 0; i < 8; i++) {
                board[0][i] = "B" + backRank[i];
                board[1][i] = "BPA";
                board[6][i] = "WPA";
                board[7][i] = "W" + backRank[i];
            }

            for (int i = 2; i <= 5; i++) {
                Arrays.fill(board[i], "--");
            }
        }

        public void printBoard() {
            System.out.println("Initial Chess Board Setup:\n");
            for (int i = 7; i >= 0; i--) {
                System.out.print((i + 1) + "  ");
                for (int j = 0; j < 8; j++) {
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println("\n    a   b   c   d   e   f   g   h\n");
        }
    }

    // ---------------- Move Validator (Stub) ----------------
    static class MoveValidator {
        public static boolean isValidMove(Board board, String move, boolean isWhite) {
            // Stub: validate based on current board state
            return true;
        }
    }
}
