package com.portablegame.testing;

import com.portablegame.main.model.Bishop;
import com.portablegame.main.model.Board;
import com.portablegame.main.model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BishopTest {
    private Board board;
    private Bishop bishop;

    @BeforeEach
    public void setUp() {
        board = new Board();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board.setPieceAt(i, j, null);
            }
        }
        bishop = new Bishop("white", 3, 3, board);
        board.setPieceAt(3, 3, bishop);
    }

    @Test
    public void testValidMoves() {
        assertTrue(bishop.isValidMove(0, 0));
        assertTrue(bishop.isValidMove(7, 7));
        assertTrue(bishop.isValidMove(0, 6));
        assertTrue(bishop.isValidMove(6, 0));
    }

    @Test
    public void testBlockedPath() {
        board.setPieceAt(4, 4, new Pawn("white", 4, 4, board));
        assertFalse(bishop.isValidMove(5, 5));

        board.setPieceAt(4, 4, new Pawn("black", 4, 4, board));
        assertTrue(bishop.isValidMove(4, 4));
    }

    @Test
    public void testInvalidMoves() {
        assertFalse(bishop.isValidMove(3, 0));
        assertFalse(bishop.isValidMove(0, 3));
        assertFalse(bishop.isValidMove(4, 5));
    }
}