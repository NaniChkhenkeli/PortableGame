package com.portablegame.util;

import com.portablegame.main.model.*;
import java.util.ArrayList;
import java.util.List;

public class GameValidator {
    private Board board;
    private boolean whiteToMove;
    private List<String> moveHistory;
    private String currentColor;

    public GameValidator() {
        this.board = new Board();
        this.whiteToMove = true;
        this.moveHistory = new ArrayList<>();
    }

    public ValidationResult validateGame(List<String> moves) {
        board.initializeBoard();
        moveHistory.clear();
        whiteToMove = true;
        currentColor = "white";

        ValidationResult result = new ValidationResult();
        int moveNumber = 1;

        for (String move : moves) {
            if (move.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.gameResult = move;
                continue;
            }

            MoveValidation moveValidation = validateMove(move, moveNumber);
            if (!moveValidation.isValid) {
                result.valid = false;
                result.errors.addAll(moveValidation.errors);
                return result;
            }

            executeValidatedMove(moveValidation);
            moveHistory.add(move);
            whiteToMove = !whiteToMove;
            currentColor = whiteToMove ? "white" : "black";
            moveNumber++;
        }

        result.valid = true;
        return result;
    }

    private MoveValidation validateMove(String move, int moveNumber) {
        MoveValidation validation = new MoveValidation();
        validation.moveNumber = moveNumber;
        validation.originalNotation = move;
        validation.color = currentColor;

        if (move.equals("O-O") || move.equals("O-O-O")) {
            return validateCastling(move, validation);
        }

        if (move.contains("=")) {
            validation.addError("Promotion not yet implemented");
            return validation;
        }

        MoveCoordinates coords = convertAlgebraicNotation(move);
        if (coords == null) {
            validation.addError("Invalid move notation: " + move);
            return validation;
        }

        Piece piece = board.getPieceAt(coords.from);
        if (piece == null) {
            validation.addError("No piece at " + coords.from);
            return validation;
        }

        if (!piece.getColor().equals(currentColor)) {
            validation.addError("Wrong color piece at " + coords.from);
            return validation;
        }

        int fromRow = 8 - Character.getNumericValue(coords.from.charAt(1));
        int fromCol = coords.from.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(coords.to.charAt(1));
        int toCol = coords.to.charAt(0) - 'a';

        if (!piece.isValidMove(toRow, toCol)) {
            validation.addError("Illegal move for " + piece.getClass().getSimpleName());
            return validation;
        }

        if ((piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) &&
                !isPathClear(fromRow, fromCol, toRow, toCol)) {
            validation.addError("Path is not clear");
            return validation;
        }

        if (move.contains("x")) {
            Piece target = board.getPieceAt(coords.to);
            if (target == null) {
                validation.addError("No piece to capture at " + coords.to);
                return validation;
            }
        }

        validation.coordinates = coords;
        validation.isValid = true;
        return validation;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board.getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }

    private MoveValidation validateCastling(String move, MoveValidation validation) {
        String kingPos = validation.color.equals("white") ? "e1" : "e8";
        String rookPos = move.equals("O-O") ?
                (validation.color.equals("white") ? "h1" : "h8") :
                (validation.color.equals("white") ? "a1" : "a8");

        Piece king = board.getPieceAt(kingPos);
        Piece rook = board.getPieceAt(rookPos);

        if (!(king instanceof King) || !(rook instanceof Rook)) {
            validation.addError("Castling pieces missing");
            return validation;
        }

        if (validation.color.equals("white") ? board.isWhiteKingMoved() : board.isBlackKingMoved()) {
            validation.addError("King has already moved");
            return validation;
        }

        if (move.equals("O-O") &&
                (validation.color.equals("white") ? board.isWhiteKingsideRookMoved() : board.isBlackKingsideRookMoved())) {
            validation.addError("Kingside rook has moved");
            return validation;
        }

        if (move.equals("O-O-O") &&
                (validation.color.equals("white") ? board.isWhiteQueensideRookMoved() : board.isBlackQueensideRookMoved())) {
            validation.addError("Queenside rook has moved");
            return validation;
        }

        validation.coordinates = new MoveCoordinates();
        validation.coordinates.from = kingPos;
        validation.coordinates.to = move.equals("O-O") ?
                (validation.color.equals("white") ? "g1" : "g8") :
                (validation.color.equals("white") ? "c1" : "c8");
        validation.isValid = true;
        return validation;
    }

    private void executeValidatedMove(MoveValidation validation) {
        if (validation.coordinates != null) {
            String from = validation.coordinates.from;
            String to = validation.coordinates.to;

            int fromRow = 8 - Character.getNumericValue(from.charAt(1));
            int fromCol = from.charAt(0) - 'a';
            int toRow = 8 - Character.getNumericValue(to.charAt(1));
            int toCol = to.charAt(0) - 'a';

            Piece piece = board.getPieceAt(fromRow, fromCol);
            board.setPieceAt(toRow, toCol, piece);
            board.setPieceAt(fromRow, fromCol, null);
            piece.setPosition(toRow, toCol);

            if (piece instanceof King || piece instanceof Rook) {
                updateCastlingStatus(piece, from);
            }

            if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
                board.setPieceAt(toRow, toCol, new Queen(piece.getColor(), toRow, toCol, board));
            }
        }
    }

    private void updateCastlingStatus(Piece piece, String from) {
        if (piece instanceof King) {
            if (piece.getColor().equals("white")) {
                board.setWhiteKingMoved(true);
            } else {
                board.setBlackKingMoved(true);
            }
        } else if (piece instanceof Rook) {
            if (piece.getColor().equals("white")) {
                if (from.equals("a1")) board.setWhiteQueensideRookMoved(true);
                if (from.equals("h1")) board.setWhiteKingsideRookMoved(true);
            } else {
                if (from.equals("a8")) board.setBlackQueensideRookMoved(true);
                if (from.equals("h8")) board.setBlackKingsideRookMoved(true);
            }
        }
    }

    private MoveCoordinates convertAlgebraicNotation(String move) {
        MoveCoordinates coords = new MoveCoordinates();

        if (move.matches("[a-h][1-8]") || move.matches("[a-h]x[a-h][1-8]")) {
            coords.to = move.length() == 2 ? move : move.substring(2);
            coords.from = findPawnSource(coords.to.charAt(0), coords.to.charAt(1),
                    currentColor, move.contains("x"));
            return coords.from != null ? coords : null;
        }

        if (move.matches("[KQRNB][a-h][1-8]") || move.matches("[KQRNB]x[a-h][1-8]")) {
            coords.to = move.length() == 3 ? move.substring(1) : move.substring(2);
            coords.from = findPieceSource(move.charAt(0), coords.to, currentColor);
            return coords.from != null ? coords : null;
        }

        return null;
    }

    private String findPawnSource(char file, char rank, String color, boolean isCapture) {
        int direction = color.equals("white") ? -1 : 1;
        int targetRow = 8 - Character.getNumericValue(rank);
        int targetCol = file - 'a';

        if (isCapture) {
            for (int colOffset = -1; colOffset <= 1; colOffset += 2) {
                int sourceCol = targetCol + colOffset;
                if (sourceCol >= 0 && sourceCol < 8) {
                    int sourceRow = targetRow - direction;
                    if (sourceRow >= 0 && sourceRow < 8) {
                        Piece piece = board.getPieceAt(sourceRow, sourceCol);
                        if (piece instanceof Pawn && piece.getColor().equals(color)) {
                            return "" + (char)('a' + sourceCol) + (8 - sourceRow);
                        }
                    }
                }
            }
        } else {
            int sourceRow = targetRow - direction;
            if (sourceRow >= 0 && sourceRow < 8) {
                Piece piece = board.getPieceAt(sourceRow, targetCol);
                if (piece instanceof Pawn && piece.getColor().equals(color)) {
                    return "" + file + (8 - sourceRow);
                }
            }
        }
        return null;
    }

    private String findPieceSource(char pieceType, String targetPos, String color) {
        int targetRow = 8 - Character.getNumericValue(targetPos.charAt(1));
        int targetCol = targetPos.charAt(0) - 'a';

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor().equals(color)) {
                    boolean typeMatches = false;
                    if (pieceType == 'K' && piece instanceof King) typeMatches = true;
                    else if (pieceType == 'Q' && piece instanceof Queen) typeMatches = true;
                    else if (pieceType == 'R' && piece instanceof Rook) typeMatches = true;
                    else if (pieceType == 'B' && piece instanceof Bishop) typeMatches = true;
                    else if (pieceType == 'N' && piece instanceof Knight) typeMatches = true;

                    if (typeMatches && piece.isValidMove(targetRow, targetCol) &&
                            isPathClear(row, col, targetRow, targetCol)) {
                        return "" + (char)('a' + col) + (8 - row);
                    }
                }
            }
        }
        return null;
    }

    public static class ValidationResult {
        public boolean valid;
        public String gameResult;
        public final List<String> errors = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }
    }

    private static class MoveValidation {
        public boolean isValid;
        public int moveNumber;
        public String originalNotation;
        public String color;
        public MoveCoordinates coordinates;
        public final List<String> errors = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
            isValid = false;
        }
    }

    private static class MoveCoordinates {
        public String from;
        public String to;
    }


}