package com.portablegame.testing;

import com.portablegame.main.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.initializeBoard();
    }

    @Test
    public void testInitialBoardSetup() {
        // Test that pieces are in correct starting positions
        assertTrue(board.getPieceAt(0, 0) instanceof Rook);
        assertTrue(board.getPieceAt(0, 1) instanceof Knight);
        assertTrue(board.getPieceAt(0, 2) instanceof Bishop);
        assertTrue(board.getPieceAt(0, 3) instanceof Queen);
        assertTrue(board.getPieceAt(0, 4) instanceof King);
        assertTrue(board.getPieceAt(0, 5) instanceof Bishop);
        assertTrue(board.getPieceAt(0, 6) instanceof Knight);
        assertTrue(board.getPieceAt(0, 7) instanceof Rook);

        for (int i = 0; i < 8; i++) {
            assertTrue(board.getPieceAt(1, i) instanceof Pawn);
            assertTrue(board.getPieceAt(6, i) instanceof Pawn);
        }
    }


    @Test
    public void testTryMoveValid() {
        assertTrue(board.tryMove("e2", "e4", null));
        assertEquals("e3", board.getEnPassantTarget());
        assertFalse(board.tryMove("e7", "e5", null));
    }

    @Test
    public void testCastling() {
        // Clear path for castling
        board.setPieceAt(7, 5, null);
        board.setPieceAt(7, 6, null);

        assertTrue(board.tryMove("e1", "g1", null));
        assertTrue(board.getPieceAt(7, 6) instanceof King);
        assertTrue(board.getPieceAt(7, 5) instanceof Rook);
    }

    @Test
    public void testEnPassant() {
        board.tryMove("e2", "e4", null);
        board.tryMove("h7", "h6", null);
        board.tryMove("e4", "e5", null);
        board.tryMove("d7", "d5", null);

        assertTrue(board.tryMove("e5", "d6", null));
        assertNull(board.getPieceAt(4, 3));
    }

    @Test
    public void testPromotion() {
        // Clear path and setup promotion scenario
        board.setPieceAt(1, 0, null);
        board.setPieceAt(6, 0, new Pawn("white", 6, 0, board));

        assertTrue(board.tryMove("a2", "a1", "Q"));
        assertTrue(board.getPieceAt(7, 0) instanceof Queen);
    }

    @Test
    public void testCheckDetection() {
        board.setPieceAt(6, 4, null);
        board.setPieceAt(1, 3, null);
        board.setPieceAt(0, 3, null);
        board.setPieceAt(3, 4, new Queen("black", 3, 4, board));

        assertTrue(board.isKingInCheck("white"));
        assertFalse(board.tryMove("e1", "e2", null));
    }
}