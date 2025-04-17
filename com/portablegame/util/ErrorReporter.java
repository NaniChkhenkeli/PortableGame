package com.portablegame.util;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ErrorReporter {
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;

    public ErrorReporter() {
        this("chess_validation_errors.log");
    }

    public ErrorReporter(String logFileName) {
        this.logFile = Paths.get(logFileName);
        initializeLogFile();
    }

    private void initializeLogFile() {
        try {
            if (!Files.exists(logFile)) {
                Files.createDirectories(logFile.getParent());
                Files.createFile(logFile);
                logSystemMessage("Initialized chess validation error log");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize error log: " + e.getMessage());
        }
    }

    public void logReport(ErrorReport report) {
        if (!report.hasErrors()) return;

        try {
            String logEntry = createLogEntry(report);
            Files.writeString(logFile, logEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write error report: " + e.getMessage());
        }
    }

    private String createLogEntry(ErrorReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append(createLogHeader(report.getGameId()));

        if (!report.hasErrors()) {
            sb.append("  Game is valid\n");
        } else {
            report.getErrors().stream()
                    .collect(Collectors.groupingBy(
                            e -> e.isCritical() ? "Critical Errors" : "Warnings"))
                    .forEach((category, errors) -> {
                        sb.append("  ").append(category).append(":\n");
                        errors.forEach(e -> sb.append("    - ").append(e.getMessage())
                                .append(formatMoveContext(e)).append("\n"));
                    });
        }

        sb.append("-".repeat(80)).append("\n");
        return sb.toString();
    }
    private String createLogHeader(String gameId) {
        return String.format("[%s] Game: %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                gameId);
    }

    private String formatMoveContext(ErrorReport.ChessError error) {
        if (error.getMoveNumber() <= 0) return "";
        return String.format(" (Move %d%s)",
                error.getMoveNumber(),
                error.getMoveText() != null ? ": " + error.getMoveText() : "");
    }

    public void logSystemMessage(String message) {
        try {
            String entry = String.format("[%s] [SYSTEM] %s%n",
                    LocalDateTime.now().format(TIMESTAMP_FORMAT),
                    message);
            Files.writeString(logFile, entry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log system message: " + e.getMessage());
        }
    }

    public Path getLogPath() {
        return logFile;
    }
}