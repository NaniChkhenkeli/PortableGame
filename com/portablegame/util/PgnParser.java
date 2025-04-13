package com.portablegame.util;

import java.util.*;
import java.util.regex.*;

public class PgnParser {
    private static final Pattern HEADER_PATTERN = Pattern.compile("^\\[(\\w+)\\s+\"([^\"]*)\"\\]$");
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "^(?<moveNum>\\d+\\.\\s*)?" +
                    "(?<move>(?<piece>[KQRBN])?(?<disambig>[a-h1-8])?(?<capture>x)?" +
                    "(?<target>[a-h][1-8])(?<promo>=[QRBN])?(?<check>[+#])?|" +
                    "(?<castle>O-O(?:-O)?[+#]?))" +
                    "(?<annotation>\\s*[!?]{1,2})?"
    );

    public static ParseResult parseGame(String gameText) {
        ParseResult result = new ParseResult();
        String[] lines = gameText.split("\\r?\\n");
        boolean inHeader = true;
        StringBuilder movesText = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[")) {
                if (!inHeader) {
                    result.addError("Header tags must appear before move text");
                }
                parseHeader(line, result);
            } else {
                inHeader = false;
                movesText.append(line).append(" ");
            }
        }

        if (inHeader) {
            result.addError("No moves found in game");
        } else {
            parseMoveText(movesText.toString(), result);
        }

        validateRequiredHeaders(result);
        return result;
    }

    private static void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            String value = matcher.group(2);
            result.headers.put(tag, value);
        } else {
            result.addError("Malformed header: " + line);
        }
    }

    private static void parseMoveText(String movesText, ParseResult result) {
        // Remove comments and variations
        String cleaned = movesText
                .replaceAll("\\{.*?\\}", "")
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\s+", " ")
                .trim();

        // Split into tokens
        String[] tokens = cleaned.split("(?<=\\s)(?=\\d+\\.)|\\s+");

        int expectedMoveNumber = 1;
        for (String token : tokens) {
            if (token.matches("\\d+\\.+")) {
                int actualMoveNum = Integer.parseInt(token.replace(".", ""));
                if (actualMoveNum != expectedMoveNumber) {
                    result.addError("Unexpected move number: " + token +
                            " (expected " + expectedMoveNumber + ")");
                }
                expectedMoveNumber++;
            }
            else if (token.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.result = token;
            }
            else if (!token.isEmpty()) {
                Matcher moveMatcher = MOVE_PATTERN.matcher(token);
                if (moveMatcher.matches()) {
                    result.moves.add(token);
                } else {
                    result.addError("Invalid move syntax: " + token);
                }
            }
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
        public Map<String, String> headers = new LinkedHashMap<>();
        public List<String> moves = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
        public String result;

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }
    }
}