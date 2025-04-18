package com.portablegame.testing;
import com.portablegame.main.model.Board;
import com.portablegame.main.model.King;
import com.portablegame.main.model.Knight;
import com.portablegame.main.model.Queen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameMechanicsTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.initializeBoard();
    }

    @Test
    public void testCheckDetection() {
        board.setPieceAt(6, 4, null);
        board.setPieceAt(1, 3, null);
        board.setPieceAt(0, 3, null);
        board.setPieceAt(3, 4, new Queen("black", 3, 4, board));

        assertTrue(board.isKingInCheck("white"));
        assertFalse(board.isKingInCheck("black"));
    }

    @Test
    public void testCheckmate() {
        board.tryMove("f2", "f3", null);
        board.tryMove("e7", "e5", null);
        board.tryMove("g2", "g4", null);
        board.tryMove("d8", "h4", null);

        assertTrue(board.isCheckmate("white"));
    }


    @Test
    public void testFiftyMoveRule() {
        for (int i = 0; i < 25; i++) {
            board.tryMove("b1", "c3", null);
            board.tryMove("b8", "c6", null);
            board.tryMove("c3", "b1", null);
            board.tryMove("c6", "b8", null);
        }

        assertTrue(board.isDraw());
    }
}