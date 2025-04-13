package com.portablegame.util;

import com.portablegame.main.model.PGNReader;
import java.io.IOException;
import java.util.List;

public class PgnGameValidator {
    private final GameValidator gameValidator;

    public PgnGameValidator() {
        this.gameValidator = new GameValidator();
    }

    public void validatePgnFile(String filePath) throws IOException {
        List<String> games = PGNReader.readGamesFromFile(filePath);
        for (int i = 0; i < games.size(); i++) {
            System.out.println("\n=== Validating Game " + (i + 1) + " ===");
            validateGame(games.get(i));
        }
    }

    private void validateGame(String gameText) {
        PGNReader.ParseResult parseResult = PGNReader.parseGame(gameText);

        if (parseResult.hasErrors()) {
            System.out.println("Syntax errors found:");
            parseResult.errors.forEach(System.out::println);
            return;
        }

        System.out.println("Game headers: " + parseResult.headers);
        GameValidator.ValidationResult validationResult =
                gameValidator.validateGame(parseResult.moves);

        if (validationResult.valid) {
            System.out.println("Game is VALID");
            if (parseResult.result != null) {
                System.out.println("Result: " + parseResult.result);
            }
        } else {
            System.out.println("Game is INVALID:");
            validationResult.errors.forEach(System.out::println);
        }
    }
}