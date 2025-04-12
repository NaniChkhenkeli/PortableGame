package com.portablegame.main.model;
import java.util.Arrays;
import com.portablegame.main.model.ChessModel.Color;
import com.portablegame.main.model.ChessModel.PieceType;
import com.portablegame.main.model.ChessModel.GameState;
import com.portablegame.main.model.ChessModel.Piece;
import com.portablegame.main.model.ChessModel.Square;


public class Board {
    private Piece[][] squares;
    private Color currentTurn;
    private GameState gameState;
    private Square enPassantTarget;
    private boolean[] castlingRights; // [whiteKingside, whiteQueenside, blackKingside, blackQueenside]

    public Board() {
        squares = new Piece[8][8];
        currentTurn = Color.WHITE;
        gameState = GameState.ONGOING;
        castlingRights = new boolean[]{true, true, true, true};
        initializeBoard();
    }

    private void initializeBoard() {
        // Clear the board
        for (Piece[] row : squares) {
            Arrays.fill(row, null);
        }

        // Set up pawns
        for (int file = 0; file < 8; file++) {

            squares[1][file] = new Piece(PieceType.PAWN, Color.WHITE);
            squares[6][file] = new Piece(PieceType.PAWN, Color.BLACK);

        }

        // Set up back ranks
        PieceType[] backRankOrder = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int file = 0; file < 8; file++) {

            squares[0][file] = new Piece(backRankOrder[file], Color.WHITE);
            squares[7][file] = new Piece(backRankOrder[file], Color.BLACK);

        }
    }

    public Piece getPiece(Square square) {
        return squares[square.getRank()][square.getFile()];
    }

    public void setPiece(Square square, Piece piece) {
        squares[square.getRank()][square.getFile()] = piece;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        currentTurn = currentTurn.opposite();
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // Additional board methods...
    public boolean isSquareEmpty(Square square) {
        return getPiece(square) == null;
    }

    public boolean isSquareAttacked(Square square, Color byColor) {
        // Implementation needed
        return false;
    }

    public boolean hasCastlingRight(Color color, boolean kingside) {
        int index = (color == Color.WHITE) ? (kingside ? 0 : 1) : (kingside ? 2 : 3);
        return castlingRights[index];
    }
}