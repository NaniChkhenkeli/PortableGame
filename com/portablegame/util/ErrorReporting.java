package com.portablegame.util;

public class ErrorReporting {
    public static String generateReport(PgnParser.ParseResult parseResult,
                                        GameValidator.ValidationResult validationResult) {
        StringBuilder report = new StringBuilder();

        // Header errors
        if (parseResult.hasErrors()) {
            report.append("=== Syntax Errors ===\n");
            for (String error : parseResult.errors) {
                report.append("• ").append(error).append("\n");
            }
        }

        // Validation errors
        if (!validationResult.valid) {
            report.append("\n=== Game Logic Errors ===\n");
            report.append(validationResult.getErrorReport());
        }

        // Game result validation
        if (validationResult.valid && !validationResult.errors.isEmpty()) {
            report.append("\n=== Result Validation ===\n");
            report.append(validationResult.getErrorReport());
        }

        return report.toString().isEmpty() ? "No errors found" : report.toString();
    }
}
