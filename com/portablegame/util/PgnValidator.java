package com.portablegame.util;

import com.portablegame.main.model.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PgnValidator {
    private final ErrorReporter errorReporter;
    private final PgnParser pgnParser;

    public PgnValidator() {
        this.errorReporter = new ErrorReporter();
        this.pgnParser = new PgnParser();
    }

    public void validatePgnFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            errorReporter.logSystemMessage("File not found: " + path.toAbsolutePath());
            throw new IOException("File not found");
        }

        System.out.println("\nValidating " + path.getFileName() + "...");
        System.out.println("==================================");

        List<String> games = pgnParser.readGamesFromFile(filePath);
        int validGames = 0;
        int invalidGames = 0;

        for (String gameText : games) {
            ErrorReport report = validateGame(gameText);
            if (report.hasErrors()) {
                invalidGames++;
                errorReporter.logReport(report);
                System.out.println(report.generateConsoleOutput());
            } else {
                validGames++;
                System.out.println(report.generateConsoleOutput());
            }
        }

        printSummary(validGames, invalidGames);
    }

    public ErrorReport validateGame(String gameText) {
        PgnParser.ParseResult parseResult = pgnParser.parseGame(gameText);
        String gameId = String.format("%s - %s vs %s",
                parseResult.headers.getOrDefault("Event", "Unknown Game"),
                parseResult.headers.getOrDefault("White", "?"),
                parseResult.headers.getOrDefault("Black", "?"));

        ErrorReport report = new ErrorReport(gameId);

        // Validate headers first
        validateHeaders(parseResult, report);

        // Validate moves
        ValidationResult validationResult = validateMoves(parseResult);
        if (!validationResult.valid) {
            for (String error : validationResult.errors) {
                report.addMoveError(ErrorType.ILLEGAL_MOVE, error,
                        validationResult.moveNumber, validationResult.moveText);
            }
            report.setFinalBoardState(validationResult.board);
        }

        // Only validate result if moves were valid
        if (validationResult.valid) {
            validateGameResult(parseResult.result, validationResult.board, report);
        }

        return report;
    }

    private void validateHeaders(PgnParser.ParseResult parseResult, ErrorReport report) {
        String[] requiredHeaders = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        for (String header : requiredHeaders) {
            if (!parseResult.headers.containsKey(header)) {
                report.addHeaderError(ErrorType.MISSING_REQUIRED_HEADER,
                        "Missing required header: [" + header + "]");
            }
        }

        if (parseResult.headers.containsKey("Result")) {
            String result = parseResult.headers.get("Result");
            if (!result.matches("1-0|0-1|1/2-1/2|\\*")) {
                report.addHeaderError(ErrorType.INVALID_HEADER_VALUE,
                        "Invalid Result value: " + result);
            }
        }

        if (parseResult.headers.containsKey("Date")) {
            String date = parseResult.headers.get("Date");
            if (!date.matches("\\d{4}\\.\\d{2}\\.\\d{2}|\\?{4}\\.\\?{2}\\.\\?{2}")) {
                report.addHeaderError(ErrorType.INVALID_HEADER_FORMAT,
                        "Invalid Date format (expected YYYY.MM.DD): " + date);
            }
        }
    }

    private ValidationResult validateMoves(PgnParser.ParseResult parseResult) {
        ValidationResult result = new ValidationResult();
        result.board = new Board();
        result.board.initializeBoard();
        result.valid = true;

        for (int i = 0; i < parseResult.moves.size(); i++) {
            String moveText = parseResult.moves.get(i);
            String color = parseResult.getPlayerColor(i);
            int moveNumber = parseResult.getMoveNumber(i);

            try {
                if (moveText.equals("O-O") || moveText.equals("O-O-O")) {
                    boolean kingside = moveText.equals("O-O");
                    if (!result.board.tryCastle(color, kingside)) {
                        result.errors.add("Illegal castling: " + moveText);
                        result.valid = false;
                        if (result.moveNumber == 0) {
                            result.moveNumber = moveNumber;
                            result.moveText = moveText;
                        }
                        continue;
                    }
                } else {
                    MoveValidation validation = new MoveValidation(moveNumber, moveText, color);
                    if (!validation.validateMove(moveText, color, result.board)) {
                        result.errors.addAll(validation.getErrors());
                        result.valid = false;
                        if (result.moveNumber == 0) {
                            result.moveNumber = moveNumber;
                            result.moveText = moveText;
                        }
                        continue;
                    }

                    MoveCoordinates coords = validation.getCoordinates();
                    String promotion = parseResult.getPromotion(i);
                    if (!result.board.tryMove(coords.from, coords.to, promotion)) {
                        result.errors.add("Failed to execute move: " + moveText);
                        result.valid = false;
                        if (result.moveNumber == 0) {
                            result.moveNumber = moveNumber;
                            result.moveText = moveText;
                        }
                    }
                }
            } catch (Exception e) {
                result.errors.add("Error processing move: " + e.getMessage());
                result.valid = false;
                if (result.moveNumber == 0) {
                    result.moveNumber = moveNumber;
                    result.moveText = moveText;
                }
            }
        }

        return result;
    }

    private void validateGameResult(String result, Board board, ErrorReport report) {
        if (result == null || result.equals("*")) {
            return; // Ongoing game, no result to validate
        }

        switch (result) {
            case "1-0":
                if (!board.isCheckmate("black") && !board.isResignation("black")) {
                    report.addGameStateError(ErrorType.INCORRECT_GAME_RESULT,
                            "Game claims white wins but no checkmate/resignation occurred");
                }
                break;
            case "0-1":
                if (!board.isCheckmate("white") && !board.isResignation("white")) {
                    report.addGameStateError(ErrorType.INCORRECT_GAME_RESULT,
                            "Game claims black wins but no checkmate/resignation occurred");
                }
                break;
            case "1/2-1/2":
                if (!board.isDraw()) {
                    report.addGameStateError(ErrorType.INCORRECT_GAME_RESULT,
                            "Game claims draw but no draw condition met");
                }
                break;
        }
    }

    private void printSummary(int valid, int invalid) {
        System.out.println("\nValidation Summary:");
        System.out.println("====================");
        System.out.printf("Total games processed: %d%n", valid + invalid);
        System.out.printf("Valid games: %d%n", valid);
        System.out.printf("Invalid games: %d%n", invalid);
        System.out.printf("Error log written to: %s%n", errorReporter.getLogPath());
    }

    private static class ValidationResult {
        boolean valid = true;
        Board board;
        List<String> errors = new ArrayList<>();
        int moveNumber;
        String moveText;
    }
}