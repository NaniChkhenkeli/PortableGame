package com.portablegame.testing;

import com.portablegame.main.model.Board;
import com.portablegame.main.model.Knight;
import com.portablegame.main.model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KnightTest {
    private Board board;
    private Knight knight;

    @BeforeEach
    public void setUp() {
        board = new Board();
        knight = new Knight("white", 3, 3, board);
        board.setPieceAt(3, 3, knight);
    }

    @Test
    public void testValidMoves() {
        assertTrue(knight.isValidMove(1, 2));
        assertTrue(knight.isValidMove(1, 4));
        assertTrue(knight.isValidMove(5, 2));
        assertTrue(knight.isValidMove(5, 4));
        assertTrue(knight.isValidMove(2, 1));
        assertTrue(knight.isValidMove(2, 5));
        assertTrue(knight.isValidMove(4, 1));
        assertTrue(knight.isValidMove(4, 5));
    }

    @Test
    public void testInvalidMoves() {
        assertFalse(knight.isValidMove(3, 4));
        assertFalse(knight.isValidMove(4, 3));
        assertFalse(knight.isValidMove(4, 4));
        assertFalse(knight.isValidMove(0, 0));
    }

    @Test
    public void testJumpOverPieces() {
        board.setPieceAt(2, 3, new Pawn("white", 2, 3, board));
        board.setPieceAt(3, 4, new Pawn("black", 3, 4, board));
        assertTrue(knight.isValidMove(1, 4));
    }


}