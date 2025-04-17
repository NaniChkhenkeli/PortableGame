package com.portablegame.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class PgnParser {
    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "^\\[(?<tagName>\\w+)\\s+\"(?<tagValue>(?:\\\\\"|[^\"])*)\"\\]$"
    );
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile(
            "^(?<moveNum>\\d+)\\.+(?<moveSuffix>\\s|\\.\\.)?"
    );
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "(?<move>" +
                    "(?<piece>[KQRBNP])?(?<disambigFile>[a-h])?(?<disambigRank>[1-8])?(?<capture>x)?" +
                    "(?<target>[a-h][1-8])(?<promo>=[QRBN])?(?<check>[+#])?|" +
                    "(?<castle>O-O(?:-O)?[+#]?)" +
                    ")(?<annotation>[!?]{1,2})?"
    );
    private static final Pattern GAME_RESULT = Pattern.compile(
            "^(1-0|0-1|1/2-1/2|\\*)$"
    );
    private static final Pattern NAG_PATTERN = Pattern.compile(
            "\\$(?<nag>\\d+)"
    );
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "\\{(?<comment>(?:\\\\\\}|[^\\}])*)\\}"
    );
    private static final Pattern VARIATION_PATTERN = Pattern.compile(
            "\\((?<variation>(?:\\\\\\)|[^\\)])*)\\)"
    );

    // Required headers with descriptions
    private static final Map<String, String> REQUIRED_HEADERS = Map.of(
            "Event", "Name of the tournament or match",
            "Site", "Location of the event",
            "Date", "Starting date of the game",
            "Round", "Playing round ordinal",
            "White", "Player of the white pieces",
            "Black", "Player of the black pieces",
            "Result", "Game result"
    );

    // Recommended headers
    private static final Set<String> RECOMMENDED_HEADERS = Set.of(
            "WhiteTitle", "BlackTitle", "WhiteElo", "BlackElo",
            "WhiteFideId", "BlackFideId", "ECO", "Opening",
            "Variation", "EventDate", "TimeControl", "Termination"
    );

    /**
     * Reads multiple games from a PGN file
     */
    public static List<String> readGamesFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        String content = Files.readString(path);
        return splitPgnGames(content);
    }

    // Add this new method for stream input
    public static List<String> readGamesFromStream(InputStream is) throws IOException {
        String content = new String(is.readAllBytes());
        return splitPgnGames(content);
    }

    /**
     * Splits PGN content into individual games
     */
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

    /**
     * Parses a single PGN game with advanced features
     */
    public static ParseResult parseGame(String gameText) {
        ParseResult result = new ParseResult();
        String[] lines = gameText.split("\\r?\\n");
        StringBuilder movesText = new StringBuilder();
        boolean inHeaderSection = true;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[")) {
                if (!inHeaderSection) {
                    result.addError("Header tags must appear before move text");
                }
                parseHeader(line, result);
            } else {
                inHeaderSection = false;
                movesText.append(line).append(" ");
            }
        }

        if (movesText.length() > 0) {
            processMoveText(movesText.toString(), result);
        } else if (!result.headers.isEmpty()) {
            result.addError("No moves found in game");
        }

        validateHeaders(result);
        return result;
    }

    /**
     * Parses a PGN header with escape sequence support
     */
    private static void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            String tagName = matcher.group("tagName");
            String tagValue = matcher.group("tagValue")
                    .replace("\\\"", "\""); // Unescape quotes

            // Standardize tag names (capitalize first letter, lowercase rest)
            tagName = tagName.substring(0, 1).toUpperCase() +
                    tagName.substring(1).toLowerCase();

            result.headers.put(tagName, tagValue);
        } else {
            result.addError("Malformed header: " + line);
        }
    }


    private void validateRequiredHeaders(ParseResult result) {
        String[] requiredTags = {
                "Event", "Site", "Date",
                "Round", "White", "Black", "Result"
        };

        for (String tag : requiredTags) {
            if (!result.headers.containsKey(tag)) {
                result.addError("Missing required tag: [" + tag + "]");
            }
        }

        // Validate Result tag specifically
        if (result.headers.containsKey("Result")) {
            String resultValue = result.headers.get("Result");
            if (!resultValue.matches("^(1-0|0-1|1/2-1/2|\\*)$")) {
                result.addError("Invalid Result value: " + resultValue);
            }
        }
    }

    /**
     * Processes the move text with support for comments, variations, and NAGs
     */
    private static void processMoveText(String movesText, ParseResult result) {
        // First pass: Extract and remove comments/variations
        movesText = COMMENT_PATTERN.matcher(movesText).replaceAll("");
        movesText = VARIATION_PATTERN.matcher(movesText).replaceAll("");

        // Second pass: Process NAGs (numeric annotation glyphs)
        movesText = NAG_PATTERN.matcher(movesText).replaceAll("");

        // Normalize whitespace
        String cleaned = movesText.replaceAll("\\s+", " ").trim();

        // Split into tokens while preserving move numbers
        String[] tokens = cleaned.split("(?<=\\s)(?=\\d+\\.)|\\s+");
        int currentMoveNumber = 0;
        boolean expectingWhite = true;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // Handle move numbers (e.g. "1.", "2...", "3. ")
            Matcher moveNumMatcher = MOVE_NUMBER_PATTERN.matcher(token);
            if (moveNumMatcher.find()) {
                int moveNum = Integer.parseInt(moveNumMatcher.group("moveNum"));
                if (moveNum != currentMoveNumber + 1) {
                    result.addError(String.format(
                            "Unexpected move number: %d (expected %d)",
                            moveNum, currentMoveNumber + 1
                    ));
                }
                currentMoveNumber = moveNum;
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

                // Store additional move information
                if (moveMatcher.group("promo") != null) {
                    result.promotions.add(moveMatcher.group("promo").substring(1));
                }
                if (moveMatcher.group("check") != null) {
                    result.checks.add(result.moves.size() - 1);
                }
                if (moveMatcher.group("castle") != null) {
                    result.castlingMoves.add(result.moves.size() - 1);
                }
            } else {
                result.addError("Invalid move syntax: " + token);
            }
        }

        // Validate move count matches move numbers
        int expectedMoves = currentMoveNumber * 2 - (expectingWhite ? 1 : 0);
        if (expectedMoves != result.moves.size()) {
            result.addError(String.format(
                    "Move count doesn't match move numbers (expected %d, got %d)",
                    expectedMoves, result.moves.size()
            ));
        }
    }


    /**
     * Reads headers only from all PGN games in a file.
     */
    public static List<Map<String, String>> readGameHeadersOnly(String filePath) throws IOException {
        List<String> games = readGamesFromFile(filePath);
        List<Map<String, String>> headersList = new ArrayList<>();
        for (String game : games) {
            ParseResult result = parseGame(game);
            headersList.add(result.headers);
        }
        return headersList;
    }

    /**
     * Reads only the first PGN game from a file.
     */
    public static String readFirstGame(String filePath) throws IOException {
        List<String> games = readGamesFromFile(filePath);
        return games.isEmpty() ? null : games.get(0);
    }

    /**
     * Reads all PGN files in a directory and returns all games from them.
     */
    public static List<String> readGamesFromDirectory(String dirPath) throws IOException {
        List<String> allGames = new ArrayList<>();
        Files.walk(Path.of(dirPath))
                .filter(path -> path.toString().toLowerCase().endsWith(".pgn"))
                .forEach(path -> {
                    try {
                        allGames.addAll(readGamesFromFile(path.toString()));
                    } catch (IOException e) {
                        System.err.println("Failed to read " + path + ": " + e.getMessage());
                    }
                });
        return allGames;
    }

    /**
     * Filters games by a specific header tag and value.
     */
    public static List<ParseResult> filterGamesByHeader(String filePath, String tag, String value) throws IOException {
        List<ParseResult> filtered = new ArrayList<>();
        for (String game : readGamesFromFile(filePath)) {
            ParseResult result = parseGame(game);
            if (value.equalsIgnoreCase(result.headers.getOrDefault(tag, ""))) {
                filtered.add(result);
            }
        }
        return filtered;
    }


    /**
     * Validates required headers and checks recommended ones
     */
    // Fix method declaration
    private static void validateHeaders(ParseResult result) {
        String[] requiredTags = {
                "Event", "Site", "Date", "Round", "White", "Black", "Result"
        };

        for (String tag : requiredTags) {
            if (!result.headers.containsKey(tag)) {
                result.addError("Missing required tag: [" + tag + "]");
            }
        }

        if (result.headers.containsKey("Result")) {
            String resultValue = result.headers.get("Result");
            if (!resultValue.matches("^(1-0|0-1|1/2-1/2|\\*)$")) {
                result.addError("Invalid Result value: " + resultValue);
            }
        }
    }


    private static void validateEloRating(ParseResult result, String header) {
        if (result.headers.containsKey(header)) {
            String elo = result.headers.get(header);
            if (!elo.matches("\\d{1,4}|\\?+")) {
                result.addError("Invalid " + header + " value: " + elo);
            }
        }
    }

    /**
     * Comprehensive parse result with enhanced information
     */
    public static class ParseResult {
        public final Map<String, String> headers = new LinkedHashMap<>();
        public final List<String> moves = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();
        public final List<Integer> checks = new ArrayList<>();
        public final List<Integer> castlingMoves = new ArrayList<>();
        public final List<String> promotions = new ArrayList<>();
        public String result;

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public String getPlayerColor(int moveIndex) {
            return moveIndex % 2 == 0 ? "white" : "black";
        }

        public int getMoveNumber(int moveIndex) {
            return (moveIndex / 2) + 1;
        }

        public boolean isCheck(int moveIndex) {
            return checks.contains(moveIndex);
        }

        public boolean isCastling(int moveIndex) {
            return castlingMoves.contains(moveIndex);
        }

        public String getPromotion(int moveIndex) {
            int promoIndex = moves.subList(0, moveIndex + 1).stream()
                    .filter(m -> m.contains("="))
                    .toList().size() - 1;
            return promoIndex >= 0 && promoIndex < promotions.size() ?
                    promotions.get(promoIndex) : null;
        }
        public String getHeader(String name) {
            return headers.getOrDefault(name, "");
        }

        public String getResult() {
            return result != null ? result : headers.getOrDefault("Result", "*");
        }

        @Override
        public String toString() {
            return String.format("Headers: %s\nMoves: %s\nErrors: %s\nWarnings: %s",
                    headers, moves, errors, warnings);
        }


        public String getMoveSAN(int moveIndex) {
            if (moveIndex < 0 || moveIndex >= moves.size()) return null;
            return moves.get(moveIndex);
        }

        public String getGameInfo() {
            return String.format("%s vs %s, %s (%s)",
                    headers.getOrDefault("White", "?"),
                    headers.getOrDefault("Black", "?"),
                    headers.getOrDefault("Event", "?"),
                    headers.getOrDefault("Date", "?")
            );
        }
    }

    // Utility methods for external use
    public static boolean isValidPgn(String pgnText) {
        ParseResult result = parseGame(pgnText);
        return !result.hasErrors();
    }

    public static String getGameSummary(String pgnText) {
        ParseResult result = parseGame(pgnText);
        StringBuilder summary = new StringBuilder();

        summary.append(result.getGameInfo()).append("\n");
        summary.append("Result: ").append(result.result).append("\n");
        summary.append("Moves: ").append(result.moves.size()).append("\n");

        if (result.hasErrors()) {
            summary.append("\nErrors:\n");
            result.errors.forEach(e -> summary.append("- ").append(e).append("\n"));
        }

        if (result.hasWarnings()) {
            summary.append("\nWarnings:\n");
            result.warnings.forEach(w -> summary.append("- ").append(w).append("\n"));
        }

        return summary.toString();
    }
}