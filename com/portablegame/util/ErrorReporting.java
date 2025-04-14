package com.portablegame.util;

public class ErrorReporting {
    public static String generateSummary(ErrorReport report) {
        StringBuilder summary = new StringBuilder();
        summary.append("Validation Report for Game: ").append(report.getGameId()).append("\n");
        summary.append("=".repeat(50)).append("\n");

        if (!report.hasErrors()) {
            summary.append("No errors found - game is valid\n");
            return summary.toString();
        }

        summary.append("Error Summary:\n");
        report.getErrors().forEach(error -> {
            summary.append("- ").append(error.getClass().getSimpleName())
                    .append(": ").append(error.getMessage());
            if (error.getMoveNumber() > 0) {
                summary.append(" [Move ").append(error.getMoveNumber());
                if (error.getMoveText() != null) {
                    summary.append(": ").append(error.getMoveText());
                }
                summary.append("]");
            }
            summary.append("\n");
        });

        return summary.toString();
    }

    public static String generateConsoleReport(ErrorReport report) {
        StringBuilder consoleOutput = new StringBuilder();

        if (report.hasErrors()) {
            consoleOutput.append("\u001B[31m"); // Red color for errors
            consoleOutput.append("Validation Errors for ").append(report.getGameId()).append(":\n");

            report.getErrors().forEach(error -> {
                consoleOutput.append("• ").append(error.getMessage());
                if (error.getMoveNumber() > 0) {
                    consoleOutput.append(" (Move ").append(error.getMoveNumber());
                    if (error.getMoveText() != null) {
                        consoleOutput.append(": ").append(error.getMoveText());
                    }
                    consoleOutput.append(")");
                }
                consoleOutput.append("\n");
            });
            consoleOutput.append("\u001B[0m"); // Reset color
        } else {
            consoleOutput.append("\u001B[32m"); // Green color for success
            consoleOutput.append(report.getGameId()).append(": VALID\n");
            consoleOutput.append("\u001B[0m"); // Reset color
        }

        return consoleOutput.toString();
    }
}