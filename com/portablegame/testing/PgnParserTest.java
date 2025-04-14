package com.portablegame.testing;

import com.portablegame.main.model.Board;
import com.portablegame.util.*;
import java.util.List;

public class PgnParserTest {
    public static void main(String[] args) {
        testHeaderValidation();
        testMoveValidation();
        testRequiredTags();
    }

    private static void testHeaderValidation() {
        String badHeader = "[Event \"Some Event\"]\n" +  // Fixed missing closing ]
                "[InvalidTag \"value\"]\n" +    // Removed hyphen from tag name
                "[Date \"2023.13.01\"]";        // Added quotes

        PgnParser parser = new PgnParser();
        PgnParser.ParseResult result = parser.parseGame(badHeader);

        System.out.println("Header Validation Test:");
        if (result.hasErrors()) {
            result.errors.forEach(System.out::println);
        }
    }

    private static void testMoveValidation() {
        String badMoves = "1. e4 e5\n" +
                "2. Nf3 Nc6\n" +
                "3. Bb5 a6\n" +
                "4. Bxc6 dxc6\n" +
                "5. O-O-O\n" +  // Invalid castling (queenside not allowed here)
                "6. d4 exd4\n" +
                "7. Qxd4 Qxd4\n" +
                "8. Nxd4 Bd6\n" +
                "9. Nxc6?? bxc6\n" +
                "10. 1-0";

        // Create validator and board
        PgnValidator validator = new PgnValidator();
        Board board = new Board() {
            @Override public String getFENSymbol() { return ""; }
        };
        board.initializeBoard();

        // Parse and validate
        PgnParser parser = new PgnParser();
        PgnParser.ParseResult parseResult = parser.parseGame(badMoves);
        ErrorReport report = validator.validateGame(badMoves);

        System.out.println("\nMove Validation Test:");
        if (report.hasErrors()) {
            report.getErrors().forEach(error ->
                    System.out.println(error.getMessage()));
        }
    }

    private static void testRequiredTags() {
        String minimalGame = "[Event \"?\"]\n" +
                "[Site \"?\"]\n" +
                "[Date \"????.??.??\"]\n" +
                "[Round \"?\"]\n" +
                "[White \"?\"]\n" +
                "[Black \"?\"]\n" +
                "[Result \"*\"]\n" +
                "1. e4 e5 1-0";

        PgnValidator validator = new PgnValidator();
        ErrorReport report = validator.validateGame(minimalGame);

        System.out.println("\nRequired Tags Test:");
        if (report.hasErrors()) {
            report.getErrors().forEach(error ->
                    System.out.println(error.getMessage()));
        } else {
            System.out.println("All required tags present");
        }
    }
}