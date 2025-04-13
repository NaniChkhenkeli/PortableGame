package com.portablegame.main.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PGNReader {
    private static final Pattern HEADER_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"(.+)\"\\]");
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "([KQRBN]?[a-h]?[1-8]?x?[a-h][1-8](=[QRBN])?[+#]?|O-O(?:-O)?[+#]?)"
    );

    public static List<String> readGamesFromFile(String filePath) throws IOException {
        String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        return splitPgnGames(content);
    }

    private static List<String> splitPgnGames(String pgnContent) {
        List<String> games = new ArrayList<>();
        String[] rawGames = pgnContent.split("\\n\\n(?=\\[Event )");
        for (String game : rawGames) {
            if (!game.trim().isEmpty()) {
                games.add(game);
            }
        }
        return games;
    }

    public static ParseResult parseGame(String gameText) {
        ParseResult result = new ParseResult();
        String[] lines = gameText.split("\\n");
        StringBuilder movesText = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[")) {
                parseHeader(line, result);
            } else if (!line.isEmpty()) {
                movesText.append(line).append(" ");
            }
        }

        processMoveText(movesText.toString(), result);
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
        String cleaned = movesText.replaceAll("\\{.*?\\}", "")
                .replaceAll("\\d+\\.\\.\\.", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = cleaned.split(" ");
        for (String token : tokens) {
            if (token.matches("1-0|0-1|1/2-1/2|\\*")) {
                result.result = token;
            } else if (MOVE_PATTERN.matcher(token).matches()) {
                result.moves.add(token);
            } else if (!token.isEmpty()) {
                result.addError("Invalid move syntax: " + token);
            }
        }
    }

    public static class ParseResult {
        public final java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
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
}