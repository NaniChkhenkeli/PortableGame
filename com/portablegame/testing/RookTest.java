package com.portablegame.testing;

import com.portablegame.main.model.Board;
import com.portablegame.main.model.Pawn;
import com.portablegame.main.model.Rook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RookTest {
    private Board board;
    private Rook rook;

    @BeforeEach
    public void setUp() {
        board = new Board();
        rook = new Rook("white", 3, 3, board);
        board.setPieceAt(3, 3, rook);
    }


    @Test
    public void testInvalidMoves() {
        assertFalse(rook.isValidMove(4, 4));
        assertFalse(rook.isValidMove(2, 2));
        assertFalse(rook.isValidMove(5, 4));
    }

    @Test
    public void testBlockedPath() {
        board.setPieceAt(3, 5, new Pawn("white", 3, 5, board));
        assertFalse(rook.isValidMove(3, 6));

        board.setPieceAt(3, 5, new Pawn("black", 3, 5, board));
        assertTrue(rook.isValidMove(3, 5));
    }

    @Test
    public void testCastlingStatus() {
        assertFalse(rook.hasMoved());
        rook.moveTo(3, 4);
        assertTrue(rook.hasMoved());
    }
}