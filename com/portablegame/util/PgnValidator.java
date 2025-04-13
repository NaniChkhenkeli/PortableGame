package com.portablegame.util;

import com.portablegame.main.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;



public class PgnValidator {

    private String promotionPiece;

    public void setPromotionPiece(String piece) {
        this.promotionPiece = piece;
    }

    public String getPromotionPiece() {
        return promotionPiece;
    }

    // PGN Parsing Patterns
    private static final Pattern HEADER_PATTERN = Pattern.compile("^\\[(\\w+)\\s+\"([^\"]*)\"\\]$");
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile("^(\\d+)\\.(?:\\s|\\.\\.)?");
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "(?<move>" +
                    "(?<piece>[KQRBN])?(?<disambig>[a-h1-8])?(?<capture>x)?" +
                    "(?<target>[a-h][1-8])(?<promo>=[QRBN])?(?<check>[+#])?|" +
                    "(?<castle>O-O(?:-O)?[+#]?)" +
                    ")(?<annotation>[!?]+)?"
    );
    private static final Pattern GAME_RESULT = Pattern.compile("^(1-0|0-1|1/2-1/2|\\*)$");

    // Game state
    private final Board board;
    private final List<String> validationErrors;

    public PgnValidator() {
        this.board = new Board() {
            @Override
            public String getFENSymbol() {
                return "";
            }
        };
        this.validationErrors = new ArrayList<>();
    }

    // Main validation entry point
    public void validatePgnFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }

        String content = Files.readString(path);
        if (content.length() < 10) {
            throw new IOException("File appears empty or too short");
        }

        // Rest of your validation logic
    }

    // In PgnValidator.java
    private void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            String value = matcher.group(2);

            // Standardize tag names (case-insensitive)
            tag = tag.substring(0, 1).toUpperCase() + tag.substring(1).toLowerCase();

            result.headers.put(tag, value);
        } else {
            result.addError("Invalid header format: " + line);
        }
    }


    private void validateRequiredHeaders(ParseResult result) {
        // Standard required headers
        String[] requiredHeaders = {
                "Event", "Site", "Date", "Round",
                "White", "Black", "Result"
        };

        // Additional recommended headers
        String[] recommendedHeaders = {
                "WhiteTitle", "BlackTitle", "WhiteElo", "BlackElo",
                "ECO", "Opening", "Variation", "EventDate"
        };

        // Check required headers
        for (String header : requiredHeaders) {
            if (!result.headers.containsKey(header)) {
                result.addError("Missing required header: [" + header + "]");
            }
        }

        // Warn about missing recommended headers
        for (String header : recommendedHeaders) {
            if (!result.headers.containsKey(header)) {
                result.addError("Warning: Missing recommended header [" + header + "]");
            }
        }

        // Validate Result header format if present
        if (result.headers.containsKey("Result")) {
            String resultValue = result.headers.get("Result");
            if (!resultValue.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.addError("Invalid Result header value: " + resultValue);
            }
        }

        // Validate date format if present
        if (result.headers.containsKey("Date")) {
            String date = result.headers.get("Date");
            if (!date.matches("\\d{4}\\.\\d{2}\\.\\d{2}") &&
                    !date.equals("????.??.??")) {
                result.addError("Invalid Date format (expected YYYY.MM.DD): " + date);
            }
        }
    }

    // Core validation logic
    public ValidationResult validateGame(String gameText) {
        ParseResult parseResult = parseGame(gameText);
        ValidationResult validationResult = new ValidationResult();

        if (parseResult.hasErrors()) {
            validationResult.errors.addAll(parseResult.errors);
            validationResult.valid = false;
            return validationResult;
        }

        // Copy headers to validation result
        validationResult.headers.putAll(parseResult.headers);
        validationResult.gameResult = parseResult.result;

        // Validate moves
        board.initializeBoard();
        boolean whiteToMove = true;
        int moveNumber = 1;

        for (int i = 0; i < parseResult.moves.size(); i++) {
            String move = parseResult.moves.get(i);
            String color = whiteToMove ? "white" : "black";

            if (GAME_RESULT.matcher(move).matches()) {
                validationResult.gameResult = move;
                continue;
            }

            MoveValidation moveValidation = validateMove(move, color, moveNumber, parseResult);

            if (!moveValidation.isValid()) {
                validationResult.valid = false;
                validationResult.errors.addAll(moveValidation.getErrors());
                break;
            }

            executeValidatedMove(moveValidation);
            whiteToMove = !whiteToMove;

            if (whiteToMove) {
                moveNumber++;
            }
        }

        if (validationResult.valid) {
            validateGameResult(validationResult);
        }

        return validationResult;
    }

    // Move validation helper
    private MoveValidation validateMove(String move, String color, int moveNumber, ParseResult parseResult) {
        MoveValidation validation = new MoveValidation(moveNumber, move, color);

        try {
            if (move.equals("O-O") || move.equals("O-O-O")) {
                if (!validateCastling(move, color)) {
                    validation.addError("Invalid castling move");
                } else {
                    validation.setValid(true);
                }
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

    private boolean validateCastling(String move, String color) {
        boolean kingside = move.equals("O-O");

        if (!move.matches("O-O(?:-O)?[+#]?")) {
            validationErrors.add("Invalid castling notation: " + move);
            return false;
        }

        return board.tryCastle(color, kingside);
    }

    // Move execution
    private void executeValidatedMove(MoveValidation moveValidation) {
        MoveCoordinates coords = moveValidation.getCoordinates();
        board.tryMove(coords.getFrom(), coords.getTo(), null);
    }

    // Game result validation
    private void validateGameResult(ValidationResult result) {
        if ("1-0".equals(result.gameResult)) {
            if (!board.isCheckmate("black")) {
                result.errors.add("Game result claims white wins but not checkmate");
            }
        } else if ("0-1".equals(result.gameResult)) {
            if (!board.isCheckmate("white")) {
                result.errors.add("Game result claims black wins but not checkmate");
            }
        } else if ("1/2-1/2".equals(result.gameResult)) {
            if (!board.isDraw()) {
                result.errors.add("Game result claims draw but no draw condition met");
            }
        }
    }

    // PGN Parsing methods
    private List<String> splitPgnGames(String pgnContent) {
        List<String> games = new ArrayList<>();
        String[] rawGames = pgnContent.split("(?=\\n\\[Event )");
        for (String game : rawGames) {
            String trimmed = game.trim();
            if (!trimmed.isEmpty()) {
                games.add(trimmed);
            }
        }
        return games;
    }

    private ParseResult parseGame(String gameText) {
        ParseResult result = new ParseResult();
        String[] lines = gameText.split("\\r?\\n");
        StringBuilder movesText = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[")) {
                parseHeader(line, result);
            } else if (!line.isEmpty()) {
                movesText.append(line).append(" ");
            }
        }

        if (movesText.length() > 0) {
            processMoveText(movesText.toString(), result);
        } else {
            result.addError("No moves found in game");
        }

        validateRequiredHeaders(result);
        return result;
    }

    private void processMoveText(String movesText, ParseResult result) {
        String cleaned = movesText
                .replaceAll("\\{.*?\\}", "")
                .replaceAll(";.*", "")
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = cleaned.split("(?<=\\s)(?=\\d+\\.)|\\s+");
        int currentMoveNumber = 0;
        boolean expectingWhite = true;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            Matcher moveNumMatcher = MOVE_NUMBER_PATTERN.matcher(token);
            if (moveNumMatcher.find()) {
                currentMoveNumber = Integer.parseInt(moveNumMatcher.group(1));
                expectingWhite = true;
                continue;
            }

            if (GAME_RESULT.matcher(token).matches()) {
                result.result = token;
                continue;
            }

            Matcher moveMatcher = MOVE_PATTERN.matcher(token);
            if (moveMatcher.matches()) {
                result.moves.add(moveMatcher.group("move"));
                expectingWhite = !expectingWhite;
            } else {
                result.addError("Invalid move syntax: " + token);
            }
        }
    }

    // Helper classes
    public static class ParseResult {
        public final Map<String, String> headers = new LinkedHashMap<>();
        public final List<String> moves = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
        public String result;

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }
    }

    public static class ValidationResult {
        public boolean valid = true;
        public String gameResult;
        public final Map<String, String> headers = new LinkedHashMap<>();
        public final List<String> errors = new ArrayList<>();

        public String getErrorReport() {
            if (errors.isEmpty()) return "No errors found";

            StringBuilder report = new StringBuilder();
            if (!headers.isEmpty()) {
                report.append("Game Headers:\n");
                headers.forEach((k, v) -> report.append("  ").append(k).append(": ").append(v).append("\n"));
            }

            report.append("\nValidation Errors:\n");
            errors.forEach(e -> report.append("• ").append(e).append("\n"));

            return report.toString();
        }
    }

    private void printValidationResult(ValidationResult result) {
        System.out.println(result.getErrorReport());
        System.out.println("Game is " + (result.valid ? "VALID" : "INVALID"));
        if (result.gameResult != null) {
            System.out.println("Result: " + result.gameResult);
        }
    }
}