package com.portablegame.testing;


import com.portablegame.main.model.Board;
import com.portablegame.main.model.Pawn;
import com.portablegame.main.model.Queen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueenTest {
    private Board board;
    private Queen queen;

    @BeforeEach
    public void setUp() {
        board = new Board();
        queen = new Queen("white", 3, 3, board);
        board.setPieceAt(3, 3, queen);
    }


    @Test
    public void testInvalidMoves() {
        assertFalse(queen.isValidMove(5, 4));
        assertFalse(queen.isValidMove(1, 2));
    }

    @Test
    public void testBlockedPath() {
        board.setPieceAt(4, 4, new Pawn("white", 4, 4, board));
        assertFalse(queen.isValidMove(5, 5));
        board.setPieceAt(3, 5, new Pawn("white", 3, 5, board));
        assertFalse(queen.isValidMove(3, 6));
        board.setPieceAt(4, 4, new Pawn("black", 4, 4, board));
        assertTrue(queen.isValidMove(4, 4));
    }
}