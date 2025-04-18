package com.portablegame.testing;


import com.portablegame.main.model.Board;
import com.portablegame.main.model.King;
import com.portablegame.main.model.Queen;
import com.portablegame.main.model.Rook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KingTest {
    private Board board;
    private King king;

    @BeforeEach
    public void setUp() {
        board = new Board();
        king = new King("white", 3, 3, board);
        board.setPieceAt(3, 3, king);
    }

    @Test
    public void testValidMoves() {
        assertTrue(king.isValidMove(2, 2));
        assertTrue(king.isValidMove(2, 3));
        assertTrue(king.isValidMove(2, 4));
        assertTrue(king.isValidMove(3, 2));
        assertTrue(king.isValidMove(3, 4));
        assertTrue(king.isValidMove(4, 2));
        assertTrue(king.isValidMove(4, 3));
        assertTrue(king.isValidMove(4, 4));
    }

    @Test
    public void testInvalidMoves() {
        assertFalse(king.isValidMove(1, 1));
        assertFalse(king.isValidMove(3, 5));
        assertFalse(king.isValidMove(5, 3));

        assertFalse(king.isValidMove(5, 4));
        assertFalse(king.isValidMove(1, 2));
    }


    @Test
    public void testCheckAvoidance() {
        board.setPieceAt(1, 3, new Queen("black", 1, 3, board));
        assertTrue(board.isKingInCheck("white"));
        assertFalse(king.isValidMove(3, 3));
        assertTrue(king.isValidMove(2, 3));
        assertTrue(king.isValidMove(4, 3));
    }
}
