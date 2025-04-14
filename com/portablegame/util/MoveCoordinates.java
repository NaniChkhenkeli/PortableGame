package com.portablegame.util;

import com.portablegame.main.model.*;

public class MoveCoordinates {
    public String from;
    public String to;
    public int fromRow;
    public int fromCol;
    public int toRow;
    public int toCol;

    public MoveCoordinates() {}

    public MoveCoordinates(String from, String to) {
        this.from = from;
        this.to = to;
        if (from != null && from.length() == 2) {
            this.fromCol = from.charAt(0) - 'a';
            this.fromRow = 8 - Character.getNumericValue(from.charAt(1));
        }
        if (to != null && to.length() == 2) {
            this.toCol = to.charAt(0) - 'a';
            this.toRow = 8 - Character.getNumericValue(to.charAt(1));
        }
    }

    public static MoveCoordinates fromAlgebraic(String move, String color, Board board) {
        if (move == null || move.isEmpty()) return null;

        // Handle castling notation
        if (move.startsWith("O-O")) {
            int row = color.equals("white") ? 7 : 0;
            boolean kingside = move.equals("O-O");
            String from = "e" + (row + 1);
            String to = kingside ? "g" + (row + 1) : "c" + (row + 1);
            return new MoveCoordinates(from, to);
        }

        // Parse standard moves
        String cleanMove = move.replaceAll("[+#!?]", "");
        boolean isCapture = cleanMove.contains("x");
        String target = cleanMove.replaceAll(".*([a-h][1-8]).*", "$1");

        // Determine source square
        String source = determineSourceSquare(cleanMove, target, color, board);

        if (source == null) return null;

        return new MoveCoordinates(source, target);
    }

    private static String determineSourceSquare(String move, String target, String color, Board board) {
        // Implementation for determining source square based on piece type and disambiguation
        // This is simplified - a complete implementation would need to handle all cases
        char pieceChar = move.charAt(0);
        if (Character.isUpperCase(pieceChar)) {
            // Piece move (K, Q, R, B, N)
            return findPieceSource(pieceChar, move, target, color, board);
        } else {
            // Pawn move
            return findPawnSource(move, target, color, board);
        }
    }

    private static String findPieceSource(char pieceType, String move, String target,
                                          String color, Board board) {
        // Simplified - would need full implementation
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor().equals(color)) {
                    if (pieceMatchesType(piece, pieceType) &&
                            piece.isValidMove(targetRow, targetCol)) {
                        return "" + (char)('a' + col) + (8 - row);
                    }
                }
            }
        }
        return null;
    }

    private static boolean pieceMatchesType(Piece piece, char pieceChar) {
        switch (pieceChar) {
            case 'K': return piece instanceof King;
            case 'Q': return piece instanceof Queen;
            case 'R': return piece instanceof Rook;
            case 'B': return piece instanceof Bishop;
            case 'N': return piece instanceof Knight;
            default: return false;
        }
    }

    private static String findPawnSource(String move, String target, String color, Board board) {
        // Simplified pawn source determination
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';
        int direction = color.equals("white") ? -1 : 1;

        // Check one square forward
        int sourceRow = targetRow - direction;
        if (sourceRow >= 0 && sourceRow < 8) {
            Piece piece = board.getPieceAt(sourceRow, targetCol);
            if (piece instanceof Pawn && piece.getColor().equals(color)) {
                return "" + (char)('a' + targetCol) + (8 - sourceRow);
            }
        }

        // Check two squares forward from starting position
        if (color.equals("white") && targetRow == 4 ||
                color.equals("black") && targetRow == 3) {
            sourceRow = targetRow - 2 * direction;
            if (sourceRow >= 0 && sourceRow < 8) {
                Piece piece = board.getPieceAt(sourceRow, targetCol);
                if (piece instanceof Pawn && piece.getColor().equals(color)) {
                    return "" + (char)('a' + targetCol) + (8 - sourceRow);
                }
            }
        }

        // Check captures
        if (move.contains("x")) {
            String sourceFile = move.substring(0, 1);
            int sourceCol = sourceFile.charAt(0) - 'a';
            sourceRow = targetRow - direction;
            if (sourceRow >= 0 && sourceRow < 8 && sourceCol >= 0 && sourceCol < 8) {
                Piece piece = board.getPieceAt(sourceRow, sourceCol);
                if (piece instanceof Pawn && piece.getColor().equals(color)) {
                    return sourceFile + (8 - sourceRow);
                }
            }
        }

        return null;
    }
}