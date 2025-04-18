package com.portablegame.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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
                    "(?<piece>[KQRBNP])?" +
                    "(?<disambigFile>[a-h])?" +
                    "(?<disambigRank>[1-8])?" +
                    "(?<capture>x)?" +
                    "(?<target>[a-h][1-8])" +
                    "(?<promo>=[QRBN])?" +
                    "(?<check>[+#])?|" +
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

    public static List<String> readGamesFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        String content = Files.readString(path);
        return splitPgnGames(content);
    }

    public static List<String> readGamesFromStream(InputStream is) throws IOException {
        String content = new String(is.readAllBytes());
        return splitPgnGames(content);
    }

    private static List<String> splitPgnGames(String pgnContent) {
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

    private static void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            String tagName = matcher.group("tagName");
            String tagValue = matcher.group("tagValue")
                    .replace("\\\"", "\""); // Unescape quotes

            tagName = tagName.substring(0, 1).toUpperCase() +
                    tagName.substring(1).toLowerCase();

            result.headers.put(tagName, tagValue);
        } else {
            result.addError("Malformed header: " + line);
        }
    }

    private static void processMoveText(String movesText, ParseResult result) {
        movesText = COMMENT_PATTERN.matcher(movesText).replaceAll("");
        movesText = VARIATION_PATTERN.matcher(movesText).replaceAll("");
        movesText = NAG_PATTERN.matcher(movesText).replaceAll("");

        String cleaned = movesText.replaceAll("\\s+", " ").trim();
        String[] tokens = cleaned.split("(?<=\\s)(?=\\d+\\.)|\\s+");
        int currentMoveNumber = 0;
        boolean expectingWhite = true;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

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

            if (GAME_RESULT.matcher(token).matches()) {
                result.result = token;
                continue;
            }

            Matcher moveMatcher = MOVE_PATTERN.matcher(token);
            if (moveMatcher.matches()) {
                String move = moveMatcher.group("move");
                result.moves.add(move);

                if (!expectingWhite) {
                    currentMoveNumber++;
                }
                expectingWhite = !expectingWhite;

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

        int expectedMoves = currentMoveNumber * 2 - (expectingWhite ? 1 : 0);
        if (expectedMoves != result.moves.size()) {
            result.addError(String.format(
                    "Move count doesn't match move numbers (expected %d, got %d)",
                    expectedMoves, result.moves.size()
            ));
        }
    }

    private static void validateHeaders(ParseResult result) {
        String[] requiredHeaders = {
                "Event", "Site", "Date", "Round", "White", "Black", "Result"
        };

        for (String header : requiredHeaders) {
            if (!result.headers.containsKey(header)) {
                result.addError("Missing required header: [" + header + "]");
            }
        }

        if (result.headers.containsKey("Result")) {
            String resultValue = result.headers.get("Result");
            if (!resultValue.matches("^(1-0|0-1|1/2-1/2|\\*)$")) {
                result.addError("Invalid Result value: " + resultValue);
            }
        }
    }

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
}