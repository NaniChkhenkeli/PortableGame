package com.portablegame.main.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class PGNReader {
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

    public static List<String> readGamesFromFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        return splitPgnGames(content);
    }

    private static List<String> splitPgnGames(String pgnContent) {
        List<String> games = new ArrayList<>();
        // Split on new game markers while preserving the [Event tag
        String[] rawGames = pgnContent.split("(?=\\n\\[Event )");
        for (String game : rawGames) {
            String trimmed = game.trim();
            if (!trimmed.isEmpty()) {
                games.add(trimmed);
            }
        }
        return games;
    }

    public static ParseResult parseGame(String gameText) {
        ParseResult result = new ParseResult();
        String[] lines = gameText.split("\\r?\\n");
        StringBuilder movesText = new StringBuilder();

        // Parse headers and collect move text
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

    private static void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            result.headers.put(matcher.group(1), matcher.group(2));
        } else {
            result.addError("Invalid header format: " + line);
        }
    }

    private static void processMoveText(String movesText, ParseResult result) {
        // Remove comments and variations while preserving move text
        String cleaned = movesText
                .replaceAll("\\{.*?\\}", "")         // Remove {comments}
                .replaceAll(";.*", "")               // Remove ;comments to end of line
                .replaceAll("\\(.*?\\)", "")         // Remove (variations)
                .replaceAll("\\s+", " ")             // Normalize whitespace
                .trim();

        // Split into individual tokens while preserving move numbers
        String[] tokens = cleaned.split("(?<=\\s)(?=\\d+\\.)|\\s+");
        int currentMoveNumber = 0;
        boolean expectingWhite = true;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // Handle move numbers (e.g. "1.", "2...", "3. ")
            Matcher moveNumMatcher = MOVE_NUMBER_PATTERN.matcher(token);
            if (moveNumMatcher.find()) {
                currentMoveNumber = Integer.parseInt(moveNumMatcher.group(1));
                expectingWhite = true;
                continue;
            }

            // Handle game result
            if (GAME_RESULT.matcher(token).matches()) {
                result.result = token;
                continue;
            }

            // Parse actual moves
            Matcher moveMatcher = MOVE_PATTERN.matcher(token);
            if (moveMatcher.matches()) {
                String move = moveMatcher.group("move");
                result.moves.add(move);

                // Track whose turn it is
                if (!expectingWhite) {
                    currentMoveNumber++;
                }
                expectingWhite = !expectingWhite;
            } else {
                result.addError("Invalid move syntax: " + token);
            }
        }

        // Validate move count matches move numbers
        if (currentMoveNumber * 2 - (expectingWhite ? 1 : 0) != result.moves.size()) {
            result.addError("Move count doesn't match move numbers");
        }
    }

    private static void validateRequiredHeaders(ParseResult result) {
        String[] requiredHeaders = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        for (String header : requiredHeaders) {
            if (!result.headers.containsKey(header)) {
                result.addError("Missing required header: [" + header + "]");
            }
        }
    }

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

        public String getPlayerColor(int moveIndex) {
            return moveIndex % 2 == 0 ? "white" : "black";
        }

        public int getMoveNumber(int moveIndex) {
            return (moveIndex / 2) + 1;
        }
    }
}