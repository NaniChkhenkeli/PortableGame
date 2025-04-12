package com.portablegame.main.model;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PGNReader {

    private Board board;

    public PGNReader(Board board) {
        this.board = board;
    }

    public void readPGN(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] move = line.split(" ");
                for (String m : move) {
                    if (m.length() == 4) {
                        String from = m.substring(0, 2);
                        String to = m.substring(2);
                        board.movePiece(from, to, "white");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading PGN file: " + e.getMessage());
        }
    }
}
