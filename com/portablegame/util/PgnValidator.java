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
        ErrorReport report = new ErrorReport(
                String.format("%s - %s vs %s",
                        parseResult.headers.getOrDefault("Event", "Unknown Game"),
                        parseResult.headers.getOrDefault("White", "?"),
                        parseResult.headers.getOrDefault("Black", "?"))
        );

        validateHeaders(parseResult, report);
        if (report.hasErrors()) return report;

        ValidationResult validationResult = validateMoves(parseResult);
        if (!validationResult.valid) {
            validationResult.errors.forEach(error ->
                    report.addIllegalMoveError(error, validationResult.moveNumber, validationResult.moveText));
        }

        validateGameResult(parseResult.result, validationResult.board, report);
        return report;
    }

    private void validateHeaders(PgnParser.ParseResult parseResult, ErrorReport report) {
        String[] requiredHeaders = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        for (String header : requiredHeaders) {
            if (!parseResult.headers.containsKey(header)) {
                report.addHeaderValidationError("Missing required header: [" + header + "]");
            }
        }

        if (parseResult.headers.containsKey("Result")) {
            String result = parseResult.headers.get("Result");
            if (!result.matches("1-0|0-1|1/2-1/2|\\*")) {
                report.addHeaderValidationError("Invalid Result value: " + result);
            }
        }

        if (parseResult.headers.containsKey("Date")) {
            String date = parseResult.headers.get("Date");
            if (!date.matches("\\d{4}\\.\\d{2}\\.\\d{2}|\\?{4}\\.\\?{2}\\.\\?{2}")) {
                report.addHeaderValidationError("Invalid Date format (expected YYYY.MM.DD): " + date);
            }
        }
    }

    private ValidationResult validateMoves(PgnParser.ParseResult parseResult) {
        ValidationResult result = new ValidationResult();
        result.board = new Board() {
            @Override public String getFENSymbol() { return ""; }
        };
        result.board.initializeBoard();

        for (int i = 0; i < parseResult.moves.size(); i++) {
            String moveText = parseResult.moves.get(i);
            String color = parseResult.getPlayerColor(i);
            int moveNumber = parseResult.getMoveNumber(i);

            MoveValidation validation = new MoveValidation(moveNumber, moveText, color);
            if (!validation.validateMove(moveText, color, result.board)) {
                result.errors = validation.getErrors();
                result.valid = false;
                result.moveNumber = moveNumber;
                result.moveText = moveText;
                break;
            }

            executeValidatedMove(validation, result.board);
        }

        return result;
    }

    private void executeValidatedMove(MoveValidation validation, Board board) {
        MoveCoordinates coords = validation.getCoordinates();
        if (!board.tryMove(coords.from, coords.to, validation.getPromotionPiece())) {
            throw new IllegalStateException("Failed to execute validated move: " +
                    coords.from + " to " + coords.to);
        }
    }

    private void validateGameResult(String result, Board board, ErrorReport report) {
        if (result == null) return;

        switch (result) {
            case "1-0":
                if (!board.isCheckmate("black")) {
                    report.addGameResultError("Game claims white wins but no checkmate occurred");
                }
                break;
            case "0-1":
                if (!board.isCheckmate("white")) {
                    report.addGameResultError("Game claims black wins but no checkmate occurred");
                }
                break;
            case "1/2-1/2":
                if (!board.isDraw()) {
                    report.addGameResultError("Game claims draw but no draw condition met");
                }
                break;
        }
    }

    private void printSummary(int valid, int invalid) {
        System.out.println("\nValidation Summary:");
        System.out.println("-------------------");
        System.out.printf("Total Games: %d%n", valid + invalid);
        System.out.printf("Valid Games: %d%n", valid);
        System.out.printf("Invalid Games: %d%n", invalid);
        System.out.printf("Error log: %s%n", errorReporter.getLogPath());
    }

    private static class ValidationResult {
        boolean valid = true;
        Board board;
        List<String> errors = new ArrayList<>();
        int moveNumber;
        String moveText;
    }
}