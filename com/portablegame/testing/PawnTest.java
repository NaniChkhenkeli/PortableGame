package com.portablegame.testing;

import com.portablegame.main.model.Board;
import com.portablegame.main.model.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PawnTest {
    private Board board;
    private Pawn whitePawn;
    private Pawn blackPawn;

    @BeforeEach
    public void setUp() {
        board = new Board();
        whitePawn = new Pawn("white", 3, 3, board);
        blackPawn = new Pawn("black", 4, 4, board);
        board.setPieceAt(3, 3, whitePawn);
        board.setPieceAt(4, 4, blackPawn);
    }

    @Test
    public void testWhitePawnMoves() {
        assertTrue(whitePawn.isValidMove(2, 3));
        whitePawn.setPosition(6, 3);
        assertTrue(whitePawn.isValidMove(4, 3));
        assertFalse(whitePawn.isValidMove(7, 3));
    }

    @Test
    public void testBlackPawnMoves() {
        assertTrue(blackPawn.isValidMove(5, 4));
        blackPawn.setPosition(1, 4);
        assertTrue(blackPawn.isValidMove(3, 4));
        assertFalse(blackPawn.isValidMove(0, 4));
    }

    @Test
    public void testPawnCaptures() {
        board.setPieceAt(2, 2, new Pawn("black", 2, 2, board));
        assertTrue(whitePawn.isValidMove(2, 2));
        board.setPieceAt(2, 4, new Pawn("white", 2, 4, board));
        assertFalse(whitePawn.isValidMove(2, 4));
    }


    @Test
    public void testBlockedPawn() {
        board.setPieceAt(2, 3, new Pawn("black", 2, 3, board));
        assertFalse(whitePawn.isValidMove(2, 3));
        whitePawn.setPosition(6, 3);
        board.setPieceAt(5, 3, new Pawn("black", 5, 3, board));
        assertFalse(whitePawn.isValidMove(4, 3));
    }
}