package com.portablegame.util;

import com.portablegame.main.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Handle castling
        if (move.equals("O-O") || move.equals("O-O-O")) {
            return getCastlingCoordinates(move, color);
        }

        // Clean the move notation
        String cleanMove = move.replaceAll("[+#!?]", "");

        // Parse pawn moves
        if (cleanMove.matches("[a-h][1-8]")) {
            return parsePawnMove(cleanMove, color, board);
        }

        // Parse pawn captures
        if (cleanMove.matches("[a-h]x[a-h][1-8]")) {
            return parsePawnCapture(cleanMove, color, board);
        }

        // Parse piece moves
        Pattern pieceMovePattern = Pattern.compile("([KQRBN])([a-h]?[1-8]?)x?([a-h][1-8])");
        Matcher matcher = pieceMovePattern.matcher(cleanMove);
        if (matcher.matches()) {
            return parsePieceMove(matcher, color, board);
        }

        return null;
    }

    private static MoveCoordinates parsePawnMove(String move, String color, Board board) {
        String target = move;
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';
        int direction = color.equals("white") ? -1 : 1;
        int startRow = color.equals("white") ? 6 : 1;

        // Check for single square move
        int sourceRow = targetRow - direction;
        if (sourceRow >= 0 && sourceRow < 8) {
            Piece pawn = board.getPieceAt(sourceRow, targetCol);
            if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                return new MoveCoordinates(
                        "" + target.charAt(0) + (8 - sourceRow),
                        target
                );
            }
        }

        // Check for double move from starting position
        if (targetRow == startRow + 2 * direction) {
            Piece pawn = board.getPieceAt(startRow, targetCol);
            if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                return new MoveCoordinates(
                        "" + target.charAt(0) + (8 - startRow),
                        target
                );
            }
        }

        return null;
    }

    private static MoveCoordinates parsePawnCapture(String move, String color, Board board) {
        char sourceFile = move.charAt(0);
        String target = move.substring(2);
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';
        int sourceCol = sourceFile - 'a';
        int direction = color.equals("white") ? -1 : 1;
        int sourceRow = targetRow - direction;

        // Check for normal pawn capture
        if (sourceRow >= 0 && sourceRow < 8) {
            Piece pawn = board.getPieceAt(sourceRow, sourceCol);
            if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                return new MoveCoordinates(
                        "" + sourceFile + (8 - sourceRow),
                        target
                );
            }
        }

        // Check for en passant
        String epTarget = board.getEnPassantTarget();
        if (epTarget != null && epTarget.equals(target)) {
            int epRow = color.equals("white") ? 3 : 4;
            Piece pawn = board.getPieceAt(epRow, sourceCol);
            if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                return new MoveCoordinates(
                        "" + sourceFile + (8 - epRow),
                        target
                );
            }
        }

        return null;
    }

    private static MoveCoordinates parsePieceMove(Matcher matcher, String color, Board board) {
        String pieceType = matcher.group(1);
        String disambig = matcher.group(2);
        String target = matcher.group(3);

        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';

        Class<? extends Piece> pieceClass = getPieceClass(pieceType.charAt(0));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getClass().equals(pieceClass) &&
                        piece.getColor().equals(color)) {

                    // Check disambiguation
                    if (disambig != null && !disambig.isEmpty()) {
                        char c = disambig.charAt(0);
                        if (Character.isLetter(c)) {
                            if (col != (c - 'a')) continue;
                        } else {
                            if (row != (8 - Character.getNumericValue(c))) continue;
                        }
                    }

                    if (piece.isValidMove(targetRow, targetCol)) {
                        return new MoveCoordinates(
                                "" + (char)('a' + col) + (8 - row),
                                target
                        );
                    }
                }
            }
        }

        return null;
    }

    private static Class<? extends Piece> getPieceClass(char pieceChar) {
        switch (Character.toUpperCase(pieceChar)) {
            case 'K': return King.class;
            case 'Q': return Queen.class;
            case 'R': return Rook.class;
            case 'B': return Bishop.class;
            case 'N': return Knight.class;
            default: return Pawn.class;
        }
    }

    private static MoveCoordinates getCastlingCoordinates(String move, String color) {
        int row = color.equals("white") ? 7 : 0;
        boolean kingside = move.equals("O-O");
        String from = "e" + (8 - row);
        String to = kingside ? "g" + (8 - row) : "c" + (8 - row);
        return new MoveCoordinates(from, to);
    }
}