package com.portablegame.util;

public class ErrorReporting {
    public static String generateReport(PgnValidator.ParseResult parseResult,
                                        PgnValidator.ValidationResult validationResult) {
        StringBuilder report = new StringBuilder();
        boolean hasErrors = false;

        // 1. Header/Syntax Errors
        if (parseResult.hasErrors()) {
            report.append("=== PGN Syntax Errors ===\n");
            parseResult.errors.forEach(error ->
                    report.append("• ").append(error).append("\n"));
            hasErrors = true;
        }

        // 2. Move Validation Errors
        if (!validationResult.valid) {
            if (hasErrors) report.append("\n");
            report.append("=== Move Validation Errors ===\n");
            validationResult.errors.forEach(error ->
                    report.append("• ").append(error).append("\n"));
            hasErrors = true;
        }

        // 3. Game Result Validation
        if (validationResult.gameResult != null) {
            if (hasErrors) report.append("\n");
            report.append("=== Game Result ===\n")
                    .append("• Final Result: ").append(validationResult.gameResult);

            // Add result validation warnings if any
            if (!validationResult.valid && validationResult.errors.stream()
                    .anyMatch(e -> e.contains("result claims"))) {
                report.append("\n• Warning: Result may be incorrect - check move validation");
            }
        }

        return hasErrors ? report.toString() : "No errors found - game is valid";
    }
}