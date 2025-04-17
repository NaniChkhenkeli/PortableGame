package com.portablegame.util;

import com.portablegame.main.model.Board;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ErrorReport {
    private final String gameId;
    private final List<ChessError> errors = new ArrayList<>();
    private Board finalBoardState;

    public ErrorReport(String gameId) {
        this.gameId = Objects.requireNonNull(gameId, "Game ID cannot be null");
    }

    public interface ChessError {
        ErrorType getType();
        String getMessage();
        int getMoveNumber();
        String getMoveText();
        default boolean isCritical() {
            return getType().isCritical();
        }
    }

    public static class HeaderError implements ChessError {
        private final ErrorType type;
        private final String message;

        public HeaderError(ErrorType type, String message) {
            this.type = Objects.requireNonNull(type);
            this.message = Objects.requireNonNull(message);
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return -1; }
        @Override public String getMoveText() { return null; }
    }

    public static class MoveError implements ChessError {
        private final ErrorType type;
        private final String message;
        private final int moveNumber;
        private final String moveText;

        public MoveError(ErrorType type, String message, int moveNumber, String moveText) {
            this.type = Objects.requireNonNull(type);
            this.message = Objects.requireNonNull(message);
            this.moveNumber = moveNumber;
            this.moveText = moveText;
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return moveNumber; }
        @Override public String getMoveText() { return moveText; }
    }

    public static class GameStateError implements ChessError {
        private final ErrorType type;
        private final String message;

        public GameStateError(ErrorType type, String message) {
            this.type = Objects.requireNonNull(type);
            this.message = Objects.requireNonNull(message);
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return -1; }
        @Override public String getMoveText() { return null; }
    }

    public String getGameId() {
        return gameId;
    }

    public void addHeaderError(ErrorType type, String message) {
        if (!type.isHeaderError()) {
            throw new IllegalArgumentException("Not a header error type: " + type);
        }
        errors.add(new HeaderError(type, message));
    }

    public void addMoveError(ErrorType type, String message, int moveNumber, String moveText) {
        if (!type.isMoveError()) {
            throw new IllegalArgumentException("Not a move error type: " + type);
        }
        errors.add(new MoveError(type, message, moveNumber, moveText));
    }

    public void addGameStateError(ErrorType type, String message) {
        if (!type.isGameStateError()) {
            throw new IllegalArgumentException("Not a game state error type: " + type);
        }
        errors.add(new GameStateError(type, message));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasCriticalErrors() {
        return errors.stream().anyMatch(ChessError::isCritical);
    }

    public List<ChessError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<ChessError> getErrorsByType(ErrorType type) {
        return errors.stream()
                .filter(e -> e.getType() == type)
                .toList();
    }

    public void setFinalBoardState(Board board) {
        this.finalBoardState = board;
    }

    public Board getFinalBoardState() {
        return finalBoardState;
    }

    public String generateConsoleOutput() {
        StringBuilder sb = new StringBuilder();
        if (hasErrors()) {
            sb.append(hasCriticalErrors() ? "\u001B[31m" : "\u001B[33m"); // Red/Yellow
            sb.append("Validation Results for ").append(gameId).append(":\n");

            errors.forEach(error -> {
                sb.append(error.isCritical() ? "✗ " : "⚠ ")
                        .append(error.getMessage());
                if (error.getMoveNumber() > 0) {
                    sb.append(" (Move ").append(error.getMoveNumber());
                    if (error.getMoveText() != null) {
                        sb.append(": ").append(error.getMoveText());
                    }
                    sb.append(")");
                }
                sb.append("\n");
            });

            sb.append("\u001B[0m"); // Reset color
        } else {
            sb.append("\u001B[32m")  // Green
                    .append(gameId).append(": VALID\n")
                    .append("\u001B[0m");  // Reset
        }
        return sb.toString();
    }

    public void addHeaderValidationError(String message) {
        addHeaderError(ErrorType.MISSING_REQUIRED_HEADER, message);
    }

    public void addGameResultError(String message) {
        addGameStateError(ErrorType.INCORRECT_GAME_RESULT, message);
    }

    public void addIllegalMoveError(String message, int moveNumber, String moveText) {
        addMoveError(ErrorType.ILLEGAL_MOVE, message, moveNumber, moveText);
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Report for Game: ").append(gameId).append("\n");
        sb.append("=".repeat(60)).append("\n");

        if (errors.isEmpty()) {
            sb.append("No errors found - game is valid\n");
            return sb.toString();
        }

        sb.append("Error Summary (").append(errors.size()).append(" errors):\n");
        errors.forEach(error -> {
            sb.append("- [").append(error.getType()).append("] ")
                    .append(error.getMessage());
            if (error.getMoveNumber() > 0) {
                sb.append(" (Move ").append(error.getMoveNumber());
                if (error.getMoveText() != null) {
                    sb.append(": ").append(error.getMoveText());
                }
                sb.append(")");
            }
            sb.append("\n");
        });

        if (finalBoardState != null) {
            sb.append("\nFinal Board State:\n")
                    .append(finalBoardState.toFEN())
                    .append("\n");
        }

        return sb.toString();
    }
}