package com.portablegame.util;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                Files.createFile(logFile);
                logSystemMessage("Initialized chess validation error log");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize error log: " + e.getMessage());
        }
    }

    public void logReport(ErrorReport report) {
        if (!report.hasErrors()) return;

        StringBuilder content = new StringBuilder();
        content.append(createLogHeader(report.getGameId()));

        for (ErrorReport.ChessError error : report.getErrors()) {
            content.append(formatErrorEntry(error));
        }

        writeToLog(content.toString());
    }

    public void logSystemMessage(String message) {
        String entry = String.format("[%s] [SYSTEM] %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                message);
        writeToLog(entry);
    }

    private String createLogHeader(String gameId) {
        return String.format("%n[%s] Game: %s%n",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                gameId);
    }

    private String formatErrorEntry(ErrorReport.ChessError error) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(error.getClass().getSimpleName())
                .append(": ").append(error.getMessage());

        if (error.getMoveNumber() > 0) {
            sb.append(" [Move ").append(error.getMoveNumber());
            if (error.getMoveText() != null) {
                sb.append(": ").append(error.getMoveText());
            }
            sb.append("]");
        }
        sb.append("%n");

        return sb.toString();
    }

    private void writeToLog(String content) {
        try {
            Files.write(logFile, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write to error log: " + e.getMessage());
        }
    }

    public Path getLogPath() {
        return logFile;
    }
}