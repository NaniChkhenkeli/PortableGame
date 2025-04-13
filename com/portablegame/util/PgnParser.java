package com.portablegame.util;

import java.util.*;
import java.util.regex.*;

public class PgnParser {
    private static final Pattern HEADER_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"(.+)\"\\]");
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile("\\d+\\.");
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "([KQRBN]?[a-h]?[1-8]?x?[a-h][1-8](=[QRBN])?[+#]?|O-O(?:-O)?[+#]?)"
    );

    public static ParseResult parseGame(String pgnText) {
        ParseResult result = new ParseResult();
        String[] lines = pgnText.split("\n");
        boolean inMoveSection = false;
        List<String> moveTokens = new ArrayList<>();

        // Process each line
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[")) {
                // Header line
                inMoveSection = false;
                parseHeader(line, result);
            } else {
                // Move text line
                inMoveSection = true;
                parseMoveText(line, result, moveTokens);
            }
        }

        // Process the collected move tokens
        processMoveTokens(moveTokens, result);
        return result;
    }

    private static void parseHeader(String line, ParseResult result) {
        Matcher matcher = HEADER_PATTERN.matcher(line);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            String value = matcher.group(2);
            result.headers.put(tag, value);
        } else {
            result.addError("Invalid header format: " + line);
        }
    }

    private static void parseMoveText(String line, ParseResult result, List<String> moveTokens) {
        // Remove comments
        line = line.replaceAll("\\{.*?\\}", "").trim();
        // Split into tokens
        String[] tokens = line.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (MOVE_NUMBER_PATTERN.matcher(token).matches()) continue;

            moveTokens.add(token);
        }
    }

    private static void processMoveTokens(List<String> moveTokens, ParseResult result) {
        for (String token : moveTokens) {
            if (token.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.result = token;
            } else if (!MOVE_PATTERN.matcher(token).matches()) {
                result.addError("Invalid move syntax: " + token);
            } else {
                result.moves.add(token);
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