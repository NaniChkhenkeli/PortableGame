package com.portablegame.util;

public enum ErrorType {
    // Header errors
    MISSING_REQUIRED_HEADER,
    INVALID_HEADER_FORMAT,
    INVALID_HEADER_VALUE,

    // Move syntax errors
    INVALID_MOVE_SYNTAX,
    INVALID_MOVE_NUMBER,
    INVALID_GAME_RESULT,

    // Chess rule violations
    ILLEGAL_MOVE,
    INVALID_CASTLING,
    INVALID_EN_PASSANT,
    INVALID_PROMOTION,
    KING_IN_CHECK,
    WRONG_COLOR_TO_MOVE,

    // Game state errors
    INVALID_BOARD_STATE,
    INCORRECT_GAME_RESULT,
    INSUFFICIENT_MATERIAL,
    THREE_FOLD_REPETITION,
    FIFTY_MOVE_RULE_VIOLATION,

    // System errors
    FILE_IO_ERROR,
    PGN_PARSE_ERROR
}