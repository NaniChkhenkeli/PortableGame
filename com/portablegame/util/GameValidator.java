package com.portablegame.util;

import com.portablegame.main.model.Board;
import com.portablegame.main.model.Piece;
import com.portablegame.main.model.MoveCoordinates;
import com.portablegame.main.model.MoveValidation;

import java.util.ArrayList;
import java.util.List;

public class GameValidator {
    private final Board board;
    private boolean whiteToMove;
    private final List<String> validationErrors;

    public GameValidator() {
        this.board = new Board();
        this.validationErrors = new ArrayList<>();
    }

    public ValidationResult validateGame(List<String> moves) {
        board.initializeBoard();
        validationErrors.clear();
        whiteToMove = true;
        ValidationResult result = new ValidationResult();
        int moveNumber = 1;

        for (String move : moves) {
            if (move.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.gameResult = move;
                continue;
            }

            String color = whiteToMove ? "white" : "black";
            MoveValidation moveValidation = validateMove(move, color, moveNumber);

            if (!moveValidation.isValid()) {
                result.valid = false;
                result.errors.addAll(moveValidation.getErrors());
                break;
            }

            executeValidatedMove(moveValidation);
            whiteToMove = !whiteToMove;
            moveNumber++;
        }

        if (result.valid) {
            validateGameResult(result);
        }

        return result;
    }

    private MoveValidation validateMove(String move, String color, int moveNumber) {
        MoveValidation validation = new MoveValidation(moveNumber, move, color);

        try {
            if (move.equals("O-O") || move.equals("O-O-O")) {
                validation.setValid(true); // You should implement full castling logic here
                return validation;
            }

            MoveCoordinates coords = MoveCoordinates.fromAlgebraic(move, color);
            if (coords == null) {
                validation.addError("Invalid move notation: " + move);
                return validation;
            }

            Piece piece = board.getPieceAt(coords.getFrom());
            if (piece == null) {
                validation.addError("No piece at " + coords.getFrom());
                return validation;
            }

            if (!piece.getColor().equals(color)) {
                validation.addError("Wrong color piece at " + coords.getFrom());
                return validation;
            }

            if (!piece.isValidMove(coords.getToRow(), coords.getToCol())) {
                validation.addError("Illegal move for " + piece.getClass().getSimpleName());
                return validation;
            }

            validation.setCoordinates(coords);
            validation.setValid(true);
        } catch (Exception e) {
            validation.addError("Error validating move: " + e.getMessage());
        }

        return validation;
    }

    private void executeValidatedMove(MoveValidation moveValidation) {
        MoveCoordinates coords = moveValidation.getCoordinates();
        board.tryMove(coords.getFrom(), coords.getTo(), null);
    }

    private void validateGameResult(ValidationResult result) {
        if ("1-0".equals(result.gameResult) && board.isKingInCheck("black")) {
            if (!board.isCheckmate("black")) {
                result.errors.add("Game result claims white wins but not checkmate.");
            }
        }
        // Add logic for "0-1" and "1/2-1/2" as needed.
    }

    public static class ValidationResult {
        public boolean valid = true;
        public String gameResult;
        public final List<String> errors = new ArrayList<>();

        public String getErrorReport() {
            StringBuilder report = new StringBuilder();
            for (String error : errors) {
                report.append("• ").append(error).append("\n");
            }
            return report.toString();
        }
    }
}
