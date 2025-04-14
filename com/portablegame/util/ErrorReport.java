package com.portablegame.util;

import java.util.ArrayList;
import java.util.List;

public class ErrorReport {
    // Base Error Interface
    public interface ChessError {
        String getMessage();
        int getMoveNumber();
        String getMoveText();
    }

    // Abstract Base Error Class
    public abstract static class BaseChessError implements ChessError {
        protected final String message;
        protected final int moveNumber;
        protected final String moveText;

        protected BaseChessError(String message, int moveNumber, String moveText) {
            this.message = message;
            this.moveNumber = moveNumber;
            this.moveText = moveText;
        }

        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return moveNumber; }
        @Override public String getMoveText() { return moveText; }
    }

    // Concrete Error Classes
    public static class SyntaxError extends BaseChessError {
        public SyntaxError(String message) { super(message, -1, null); }
    }

    public static class IllegalMoveError extends BaseChessError {
        public IllegalMoveError(String message, int moveNumber, String moveText) {
            super(message, moveNumber, moveText);
        }
    }

    public static class HeaderValidationError extends BaseChessError {
        public HeaderValidationError(String message) { super(message, -1, null); }
    }

    public static class GameResultError extends BaseChessError {
        public GameResultError(String message) { super(message, -1, null); }
    }

    public static class CastlingError extends BaseChessError {
        public CastlingError(String message, int moveNumber, String moveText) {
            super(message, moveNumber, moveText);
        }
    }

    public static class PromotionError extends BaseChessError {
        public PromotionError(String message, int moveNumber, String moveText) {
            super(message, moveNumber, moveText);
        }
    }

    public static class EnPassantError extends BaseChessError {
        public EnPassantError(String message, int moveNumber, String moveText) {
            super(message, moveNumber, moveText);
        }
    }

    public static class CheckViolationError extends BaseChessError {
        public CheckViolationError(String message, int moveNumber, String moveText) {
            super(message, moveNumber, moveText);
        }
    }

    // Error Report Implementation
    private final String gameId;
    private final List<ChessError> errors = new ArrayList<>();

    public ErrorReport(String gameId) {
        this.gameId = gameId;
    }

    // Error Adding Methods
    public void addSyntaxError(String message) {
        errors.add(new SyntaxError(message));
    }

    public void addIllegalMoveError(String message, int moveNumber, String moveText) {
        errors.add(new IllegalMoveError(message, moveNumber, moveText));
    }

    public void addHeaderValidationError(String message) {
        errors.add(new HeaderValidationError(message));
    }

    public void addGameResultError(String message) {
        errors.add(new GameResultError(message));
    }

    public void addCastlingError(String message, int moveNumber, String moveText) {
        errors.add(new CastlingError(message, moveNumber, moveText));
    }

    public void addPromotionError(String message, int moveNumber, String moveText) {
        errors.add(new PromotionError(message, moveNumber, moveText));
    }

    public void addEnPassantError(String message, int moveNumber, String moveText) {
        errors.add(new EnPassantError(message, moveNumber, moveText));
    }

    public void addCheckViolationError(String message, int moveNumber, String moveText) {
        errors.add(new CheckViolationError(message, moveNumber, moveText));
    }

    // Reporting Methods
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ChessError> getErrors() {
        return new ArrayList<>(errors);
    }

    public String getGameId() {
        return gameId;
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Report for Game: ").append(gameId).append("\n");
        sb.append("=".repeat(50)).append("\n");

        if (errors.isEmpty()) {
            sb.append("No errors found\n");
            return sb.toString();
        }

        sb.append("Error Summary:\n");
        errors.forEach(error -> {
            sb.append("- ").append(error.getClass().getSimpleName())
                    .append(": ").append(error.getMessage());
            if (error.getMoveNumber() > 0) {
                sb.append(" [Move ").append(error.getMoveNumber()).append("]");
                if (error.getMoveText() != null) {
                    sb.append(": ").append(error.getMoveText());
                }
            }
            sb.append("\n");
        });

        return sb.toString();
    }

    public String generateConsoleOutput() {
        StringBuilder sb = new StringBuilder();
        if (hasErrors()) {
            sb.append("\u001B[31m"); // Red color for errors
            sb.append("Validation Errors for ").append(gameId).append(":\n");
            errors.forEach(error -> {
                sb.append("• ").append(error.getMessage());
                if (error.getMoveNumber() > 0) {
                    sb.append(" (Move ").append(error.getMoveNumber()).append(")");
                }
                sb.append("\n");
            });
            sb.append("\u001B[0m"); // Reset color
        } else {
            sb.append("\u001B[32m"); // Green color for success
            sb.append(gameId).append(": VALID\n");
            sb.append("\u001B[0m"); // Reset color
        }
        return sb.toString();
    }
}