package com.portablegame.util;

public enum ErrorType {
    // Header errors (non-critical)
    MISSING_REQUIRED_HEADER(false, true, false, false),
    INVALID_HEADER_FORMAT(false, true, false, false),
    INVALID_HEADER_VALUE(false, true, false, false),

    // Move syntax errors (critical)
    INVALID_MOVE_SYNTAX(true, false, true, false),
    INVALID_MOVE_NUMBER(true, false, true, false),
    INVALID_GAME_RESULT(true, false, false, true),

    // Chess rule violations (critical)
    ILLEGAL_MOVE(true, false, true, false),
    INVALID_CASTLING(true, false, true, false),
    INVALID_EN_PASSANT(true, false, true, false),
    INVALID_PROMOTION(true, false, true, false),
    KING_IN_CHECK(true, false, true, false),
    WRONG_COLOR_TO_MOVE(true, false, true, false),

    // Game state errors (critical)
    INVALID_BOARD_STATE(true, false, false, true),
    INCORRECT_GAME_RESULT(true, false, false, true),
    INSUFFICIENT_MATERIAL(false, false, false, true),
    THREE_FOLD_REPETITION(false, false, false, true),
    FIFTY_MOVE_RULE_VIOLATION(false, false, false, true),

    // System errors (critical)
    FILE_IO_ERROR(true, false, false, false),
    PGN_PARSE_ERROR(true, false, false, false);

    private final boolean critical;
    private final boolean headerError;
    private final boolean moveError;
    private final boolean gameStateError;

    ErrorType(boolean critical, boolean headerError, boolean moveError, boolean gameStateError) {
        this.critical = critical;
        this.headerError = headerError;
        this.moveError = moveError;
        this.gameStateError = gameStateError;
    }

    public boolean isCritical() { return critical; }
    public boolean isHeaderError() { return headerError; }
    public boolean isMoveError() { return moveError; }
    public boolean isGameStateError() { return gameStateError; }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
    }
}