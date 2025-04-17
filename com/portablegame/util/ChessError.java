package com.portablegame.util;

public interface ChessError {
    ErrorType getType();
    String getMessage();
    int getMoveNumber();
    String getMoveText();

    default boolean isCritical() {
        return getType().isCritical();
    }

    // Concrete implementations
    class HeaderError implements ChessError {
        private final ErrorType type;
        private final String message;

        public HeaderError(ErrorType type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return -1; }
        @Override public String getMoveText() { return null; }
    }

    class MoveError implements ChessError {
        private final ErrorType type;
        private final String message;
        private final int moveNumber;
        private final String moveText;

        public MoveError(ErrorType type, String message, int moveNumber, String moveText) {
            this.type = type;
            this.message = message;
            this.moveNumber = moveNumber;
            this.moveText = moveText;
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return moveNumber; }
        @Override public String getMoveText() { return moveText; }
    }

    class GameStateError implements ChessError {
        private final ErrorType type;
        private final String message;

        public GameStateError(ErrorType type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override public ErrorType getType() { return type; }
        @Override public String getMessage() { return message; }
        @Override public int getMoveNumber() { return -1; }
        @Override public String getMoveText() { return null; }
    }
}