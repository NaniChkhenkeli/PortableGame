package com.portablegame.main;

import com.portablegame.main.model.*;
import com.portablegame.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

public class Main {
    private static final ErrorReporter errorReporter = new ErrorReporter();
    private static final PgnParser pgnParser = new PgnParser();
    private static final String DEFAULT_PGN_RESOURCE = "com/portablegame/resources/pgn/basic.pgn";

    public static void main(String[] args) {
        if (args.length > 0) {
            // Use command line argument if provided
            try {
                validatePgnFile(args[0]);
            } catch (IOException e) {
                handleFileError(e, args[0]);
            }
        } else {
            // Use default resource file
            try {
                validatePgnResource(DEFAULT_PGN_RESOURCE);
            } catch (IOException e) {
                System.err.println("\nERROR: Could not load default resource file.");
                System.err.println("Please either:");
                System.err.println("1. Place your PGN file in resources/" + DEFAULT_PGN_RESOURCE);
                System.err.println("2. Or specify a PGN file path as argument:");
                System.err.println("   Usage: java Main <pgn-file>");
                System.err.println("   Example: java Main games.pgn");
                errorReporter.logSystemMessage("Default resource load failed: " + e.getMessage());
            }
        }
    }

    private static void validatePgnFile(String filePath) throws IOException {
        Path path = Paths.get(filePath).toAbsolutePath();
        System.out.println("\nValidating PGN file: " + path);

        if (!Files.exists(path)) {
            throw new IOException("File not found at: " + path);
        }

        List<String> games = pgnParser.readGamesFromFile(path.toString());
        processGames(games, "file: " + path.getFileName());
    }

    private static void validatePgnResource(String resourcePath) throws IOException {
        System.out.println("\nValidating built-in PGN resource: " + resourcePath);

        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found in classpath: " + resourcePath);
            }

            List<String> games = pgnParser.readGamesFromStream(is);
            processGames(games, "resource: " + resourcePath);
        }
    }

    private static void processGames(List<String> games, String sourceDescription) throws IOException {
        System.out.println("==================================");
        System.out.println("Found " + games.size() + " game(s) in " + sourceDescription);

        int validGames = 0;
        int invalidGames = 0;

        for (String gameText : games) {
            PgnParser.ParseResult parseResult = pgnParser.parseGame(gameText);
            String gameId = String.format("%s - %s vs %s",
                    parseResult.headers.getOrDefault("Event", "Unknown Game"),
                    parseResult.headers.getOrDefault("White", "?"),
                    parseResult.headers.getOrDefault("Black", "?"));

            System.out.println("\nProcessing game: " + gameId);

            if (!validateHeaders(parseResult, gameId)) {
                invalidGames++;
                continue;
            }

            Board board = new Board() {
                @Override public String getFENSymbol() { return ""; }
            };
            board.initializeBoard();
            System.out.println("\nInitial board state:");
            board.printBoard();

            boolean gameValid = true;
            for (int i = 0; i < parseResult.moves.size(); i++) {
                String moveText = parseResult.moves.get(i);
                String color = parseResult.getPlayerColor(i);
                int moveNumber = parseResult.getMoveNumber(i);

                System.out.printf("\nMove %d: %s (%s)", moveNumber, moveText, color);

                MoveValidation validation = new MoveValidation(moveNumber, moveText, color);
                if (!validation.validateMove(moveText, color, board)) {
                    for (String error : validation.getErrors()) {
                        errorReporter.logReport(createErrorReport(gameId, moveNumber, moveText, error));
                        System.err.println("ERROR: " + error);
                    }
                    gameValid = false;
                    break;
                }

                MoveCoordinates coords = validation.getCoordinates();
                if (!board.tryMove(coords.from, coords.to, validation.getPromotionPiece())) {
                    String error = "Failed to execute move: " + moveText;
                    errorReporter.logReport(createErrorReport(gameId, moveNumber, moveText, error));
                    System.err.println("ERROR: " + error);
                    gameValid = false;
                    break;
                }

                System.out.println("\nBoard after move:");
                board.printBoard();

                if (board.isCheckmate(color.equals("white") ? "black" : "white")) {
                    System.out.println("Checkmate!");
                    break;
                } else if (board.isKingInCheck(color.equals("white") ? "black" : "white")) {
                    System.out.println("Check!");
                }
            }

            if (gameValid && !validateGameResult(parseResult.result, board, gameId)) {
                gameValid = false;
            }

            if (gameValid) {
                System.out.println("\nGame VALID");
                validGames++;
            } else {
                invalidGames++;
            }
        }

        printValidationSummary(validGames, invalidGames, errorReporter.getLogPath());
    }

    private static void handleFileError(IOException e, String filePath) {
        System.err.println("\nERROR processing file: " + e.getMessage());
        Path path = Paths.get(filePath).toAbsolutePath();
        System.err.println("Attempted path: " + path);

        if (!Files.exists(path)) {
            System.err.println("File does not exist at this location");
        } else if (!Files.isReadable(path)) {
            System.err.println("File exists but cannot be read (check permissions)");
        }

        errorReporter.logSystemMessage("File error [" + path + "]: " + e.getMessage());
    }

    private static boolean validateHeaders(PgnParser.ParseResult parseResult, String gameId) {
        ErrorReport report = new ErrorReport(gameId);
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

        if (report.hasErrors()) {
            errorReporter.logReport(report);
            for (ErrorReport.ChessError error : report.getErrors()) {
                System.err.println("HEADER ERROR: " + error.getMessage());
            }
            return false;
        }
        return true;
    }

    private static boolean validateGameResult(String result, Board board, String gameId) {
        if (result == null) return true;

        ErrorReport report = new ErrorReport(gameId);
        boolean valid = true;

        switch (result) {
            case "1-0":
                if (!board.isCheckmate("black")) {
                    report.addGameResultError("Game claims white wins but no checkmate occurred");
                    valid = false;
                }
                break;
            case "0-1":
                if (!board.isCheckmate("white")) {
                    report.addGameResultError("Game claims black wins but no checkmate occurred");
                    valid = false;
                }
                break;
            case "1/2-1/2":
                if (!board.isDraw()) {
                    report.addGameResultError("Game claims draw but no draw condition met");
                    valid = false;
                }
                break;
        }

        if (!valid) {
            errorReporter.logReport(report);
            for (ErrorReport.ChessError error : report.getErrors()) {
                System.err.println("RESULT ERROR: " + error.getMessage());
            }
        }
        return valid;
    }

    private static ErrorReport createErrorReport(String gameId, int moveNumber, String moveText, String error) {
        ErrorReport report = new ErrorReport(gameId);
        report.addIllegalMoveError(error, moveNumber, moveText);
        return report;
    }

    private static void printValidationSummary(int validGames, int invalidGames, Path logPath) {
        System.out.println("\nValidation Summary:");
        System.out.println("====================");
        System.out.printf("Total games processed: %d%n", validGames + invalidGames);
        System.out.printf("Valid games: %d%n", validGames);
        System.out.printf("Invalid games: %d%n", invalidGames);
        System.out.printf("Error log written to: %s%n", logPath.toAbsolutePath());
    }
}