package com.portablegame.main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private Piece[][] board;
    private String enPassantTarget;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean[] whiteRooksMoved = {false, false}; // [queenside, kingside]
    private boolean[] blackRooksMoved = {false, false};
    private List<String> moveHistory = new ArrayList<>();
    private String currentPlayer = "white";
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    private Map<String, Integer> positionCounts = new HashMap<>();

    public Board() {
        this.board = new Piece[8][8];
        initializeBoard();
    }

    public void setPieceAt(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    public boolean isPromotionMove(String from, String to) {
        Piece piece = getPieceAt(from);
        if (!(piece instanceof Pawn)) return false;

        int toRow = 8 - Character.getNumericValue(to.charAt(1));
        return (piece.getColor().equals("white") && toRow == 0) ||
                (piece.getColor().equals("black") && toRow == 7);
    }

    public void initializeBoard() {
        // Clear the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }

        // Set up pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("black", 1, i, this);
            board[6][i] = new Pawn("white", 6, i, this);
        }

        // Set up other pieces
        // Black pieces
        board[0][0] = new Rook("black", 0, 0, this);
        board[0][1] = new Knight("black", 0, 1, this);
        board[0][2] = new Bishop("black", 0, 2, this);
        board[0][3] = new Queen("black", 0, 3, this);
        board[0][4] = new King("black", 0, 4, this);
        board[0][5] = new Bishop("black", 0, 5, this);
        board[0][6] = new Knight("black", 0, 6, this);
        board[0][7] = new Rook("black", 0, 7, this);

        // White pieces
        board[7][0] = new Rook("white", 7, 0, this);
        board[7][1] = new Knight("white", 7, 1, this);
        board[7][2] = new Bishop("white", 7, 2, this);
        board[7][3] = new Queen("white", 7, 3, this);
        board[7][4] = new King("white", 7, 4, this);
        board[7][5] = new Bishop("white", 7, 5, this);
        board[7][6] = new Knight("white", 7, 6, this);
        board[7][7] = new Rook("white", 7, 7, this);

        // Reset game state
        enPassantTarget = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRooksMoved = new boolean[]{false, false};
        blackRooksMoved = new boolean[]{false, false};
        moveHistory.clear();
        currentPlayer = "white";
    }

    public Piece getPieceAt(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return null;
        }
        return board[row][col];
    }

    public Piece getPieceAt(String position) {
        if (position == null || position.length() != 2) return null;
        int row = 8 - Character.getNumericValue(position.charAt(1));
        int col = position.charAt(0) - 'a';
        return getPieceAt(row, col);
    }

    public boolean isWhiteKingMoved() { return whiteKingMoved; }
    public boolean isBlackKingMoved() { return blackKingMoved; }
    public boolean isWhiteRookMoved(boolean kingside) {
        return whiteRooksMoved[kingside ? 1 : 0];
    }
    public boolean isBlackRookMoved(boolean kingside) {
        return blackRooksMoved[kingside ? 1 : 0];
    }

    public boolean tryMove(String from, String to, String promotionChoice) {
        // Validate input coordinates
        if (from == null || to == null || !from.matches("[a-h][1-8]") || !to.matches("[a-h][1-8]")) {
            return false;
        }

        // Get piece and validate ownership
        Piece piece = getPieceAt(from);
        if (piece == null || !piece.getColor().equals(currentPlayer)) {
            return false;
        }

        // Convert to board coordinates (0-based)
        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
        int fromCol = from.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));
        int toCol = to.charAt(0) - 'a';

        // Handle castling (king moving two squares)


        // Validate basic move rules
        if (!piece.isValidMove(toRow, toCol)) {
            return false;
        }

        // Handle special pawn moves
        if (piece instanceof Pawn) {
            // En passant capture
            if (to.equals(enPassantTarget) && Math.abs(fromCol - toCol) == 1) {
                executeEnPassant(fromRow, fromCol, toRow, toCol);
                return true;
            }
            // Promotion
            if ((piece.getColor().equals("white") && toRow == 0) ||
                    (piece.getColor().equals("black") && toRow == 7)) {
                if (promotionChoice == null) {
                    promotionChoice = "Q"; // Default to queen
                }
            }
        }

        // Check path is clear for sliding pieces
        if ((piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) &&
                !isPathClear(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Check if move would leave king in check
        if (wouldLeaveKingInCheck(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Execute the move
        executeMove(fromRow, fromCol, toRow, toCol, promotionChoice);
        return true;
    }



    private boolean wouldLeaveKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        // Simulate the move
        Piece movingPiece = board[fromRow][fromCol];
        Piece capturedPiece = board[toRow][toCol];

        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
        movingPiece.setPosition(toRow, toCol);

        boolean inCheck = isKingInCheck(currentPlayer);

        // Undo simulation
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        movingPiece.setPosition(fromRow, fromCol);

        return inCheck;
    }

    private void executeMove(int fromRow, int fromCol, int toRow, int toCol, String promotionChoice) {
        Piece piece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];

        // Update castling status before moving
        updateCastlingStatus(piece, fromRow, fromCol);

        // Check if a rook was captured (which affects castling rights)
        if (captured instanceof Rook) {
            if (toRow == 0 && toCol == 0) blackRooksMoved[0] = true;  // Black queenside rook
            if (toRow == 0 && toCol == 7) blackRooksMoved[1] = true;  // Black kingside rook
            if (toRow == 7 && toCol == 0) whiteRooksMoved[0] = true;  // White queenside rook
            if (toRow == 7 && toCol == 7) whiteRooksMoved[1] = true;  // White kingside rook
        }

        // Clear the source square
        board[fromRow][fromCol] = null;

        // Handle pawn promotion
        if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
            promotePawn(toRow, toCol, promotionChoice);
            piece = board[toRow][toCol]; // Get the promoted piece
        } else {
            board[toRow][toCol] = piece;
            piece.setPosition(toRow, toCol);
        }

        // Update half-move clock
        if (captured != null || piece instanceof Pawn) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        // Set en passant target
        if (piece instanceof Pawn && Math.abs(fromRow - toRow) == 2) {
            int epRow = piece.getColor().equals("white") ? toRow + 1 : toRow - 1;
            enPassantTarget = "" + (char)('a' + toCol) + (8 - epRow);
        } else {
            enPassantTarget = null;
        }

        // Update full move number after black's move
        if (currentPlayer.equals("black")) {
            fullMoveNumber++;
        }

        // Record position for threefold repetition
        String fen = toFEN();
        positionCounts.put(fen, positionCounts.getOrDefault(fen, 0) + 1);

        // Record move and switch player
        moveHistory.add(currentPlayer + ": " + positionToNotation(fromRow, fromCol) +
                (captured != null ? "x" : "-") + positionToNotation(toRow, toCol));
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    private void executeEnPassant(int fromRow, int fromCol, int toRow, int toCol) {
        Piece pawn = board[fromRow][fromCol];
        board[toRow][toCol] = pawn;
        board[fromRow][fromCol] = null;

        // Remove the captured pawn
        int capturedPawnRow = fromRow;
        int capturedPawnCol = toCol;
        board[capturedPawnRow][capturedPawnCol] = null;

        pawn.setPosition(toRow, toCol);
        enPassantTarget = null;
        moveHistory.add(currentPlayer + ": " + positionToNotation(fromRow, fromCol) +
                "x" + positionToNotation(toRow, toCol) + " e.p.");
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    public boolean tryCastle(String color, boolean kingside) {
        int row = color.equals("white") ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingside ? 7 : 0;
        int kingDestCol = kingside ? 6 : 2;
        int rookDestCol = kingside ? 5 : 3;

        // Verify king exists and hasn't moved
        Piece king = getPieceAt(row, kingCol);
        if (!(king instanceof King) || !king.getColor().equals(color)) {
            return false;
        }

        // Verify rook exists and hasn't moved
        Piece rook = getPieceAt(row, rookCol);
        if (!(rook instanceof Rook) || !rook.getColor().equals(color)) {
            return false;
        }

        // Check path is clear between king and rook
        int step = kingside ? 1 : -1;
        for (int col = kingCol + step; col != rookCol; col += step) {
            if (getPieceAt(row, col) != null) {
                return false;
            }
        }

        // Check if king is currently in check
        if (isSquareUnderAttack(row, kingCol, color.equals("white") ? "black" : "white")) {
            return false;
        }

        // Check squares king moves through aren't under attack
        for (int col = kingCol; col != kingDestCol; col += step) {
            if (col != kingCol && isSquareUnderAttack(row, col, color.equals("white") ? "black" : "white")) {
                return false;
            }
        }

        // Check destination square isn't under attack
        if (isSquareUnderAttack(row, kingDestCol, color.equals("white") ? "black" : "white")) {
            return false;
        }

        // Execute the castling
        performCastling(row, kingCol, rookCol, kingDestCol, rookDestCol, color, kingside);
        return true;
    }

    private void performCastling(int row, int kingCol, int rookCol,
                                 int kingDestCol, int rookDestCol,
                                 String color, boolean kingside) {
        // Move rook first
        Piece rook = getPieceAt(row, rookCol);
        board[row][rookDestCol] = rook;
        board[row][rookCol] = null;
        rook.setPosition(row, rookDestCol);
        if (rook instanceof Rook) {
            ((Rook)rook).setHasMoved(true);
        }

        // Then move king
        Piece king = getPieceAt(row, kingCol);
        board[row][kingDestCol] = king;
        board[row][kingCol] = null;
        king.setPosition(row, kingDestCol);
        if (king instanceof King) {
            ((King)king).setHasMoved(true);
        }

        // Update castling status
        if (color.equals("white")) {
            whiteKingMoved = true;
            if (kingside) whiteRooksMoved[1] = true;
            else whiteRooksMoved[0] = true;
        } else {
            blackKingMoved = true;
            if (kingside) blackRooksMoved[1] = true;
            else blackRooksMoved[0] = true;
        }

        // Update game state
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
        moveHistory.add(color + ": " + (kingside ? "O-O" : "O-O-O"));
    }




    private void promotePawn(int row, int col, String promotionChoice) {
        String color = board[row][col].getColor();
        board[row][col] = switch (promotionChoice != null ? promotionChoice.toUpperCase() : "Q") {
            case "R" -> new Rook(color, row, col, this);
            case "N" -> new Knight(color, row, col, this);
            case "B" -> new Bishop(color, row, col, this);
            default -> new Queen(color, row, col, this); // Default to queen
        };
    }

    // In Board.java - modify isValidCastling
    public boolean isValidCastling(String color, int row, int fromCol, int toCol) {
        // Get the king and rook pieces
        Piece king = getPieceAt(row, fromCol);
        boolean kingside = toCol > fromCol;
        int rookCol = kingside ? 7 : 0;
        Piece rook = getPieceAt(row, rookCol);

        // Basic validation
        if (!(king instanceof King) || !(rook instanceof Rook) ||
                !king.getColor().equals(color) || !rook.getColor().equals(color)) {
            return false;
        }

        // Check if pieces have moved
        if (((King)king).hasMoved() || ((Rook)rook).hasMoved()) {
            return false;
        }

        // Check if any squares between are under attack
        int step = kingside ? 1 : -1;
        for (int col = fromCol; col != toCol + step; col += step) {
            if (col != fromCol && isSquareUnderAttack(row, col, color.equals("white") ? "black" : "white")) {
                return false;
            }
        }

        // Check path is clear
        int start = Math.min(fromCol, rookCol) + 1;
        int end = Math.max(fromCol, rookCol) - 1;
        for (int col = start; col <= end; col++) {
            if (getPieceAt(row, col) != null) {
                return false;
            }
        }

        return !isKingInCheck(color); // Final check if king is currently in check
    }



    private void updateCastlingStatus(Piece piece, int row, int col) {
        if (piece instanceof King) {
            if (piece.getColor().equals("white")) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece instanceof Rook) {
            if (piece.getColor().equals("white")) {
                if (row == 7 && col == 0) whiteRooksMoved[0] = true; // Queenside
                if (row == 7 && col == 7) whiteRooksMoved[1] = true; // Kingside
            } else {
                if (row == 0 && col == 0) blackRooksMoved[0] = true;
                if (row == 0 && col == 7) blackRooksMoved[1] = true;
            }
        }
    }

    public boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        // Knights can jump over pieces
        Piece piece = board[fromRow][fromCol];
        if (piece instanceof Knight) {
            return true;
        }

        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }


    private String positionToNotation(int row, int col) {
        return "" + (char)('a' + col) + (8 - row);
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        for (int i = 0; i < 8; i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                System.out.print(piece == null ? ". " : piece.getSymbol() + " ");
            }
            System.out.println(8 - i);
        }
        System.out.println("  a b c d e f g h");
    }

    public String getEnPassantTarget() { return enPassantTarget; }
    public String getCurrentPlayer() { return currentPlayer; }
    public List<String> getMoveHistory() { return new ArrayList<>(moveHistory); }

    public boolean isSquareUnderAttack(int row, int col, String attackerColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                // Only check opponent's pieces
                if (piece != null && piece.getColor().equals(attackerColor)) {
                    if (piece.isValidMove(row, col) && isPathClear(r, c, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isDraw() {
        // Stalemate
        if (!isKingInCheck(currentPlayer) && !hasLegalMoves(currentPlayer)) {
            return true;
        }

        // Insufficient material
        if (isInsufficientMaterial()) {
            return true;
        }

        // 50-move rule
        if (halfMoveClock >= 50) {
            return true;
        }

        // Threefold repetition
        return isThreefoldRepetition();
    }

    private boolean isThreefoldRepetition() {
        return positionCounts.values().stream().anyMatch(count -> count >= 3);
    }

    public void printDebugState(String move) {
        System.out.println("\nAfter move: " + move);
        printBoard();
        System.out.println("Current player: " + currentPlayer);
        System.out.println("Castling rights:");
        System.out.println("  White: K" + (!whiteRooksMoved[1]) + " Q" + (!whiteRooksMoved[0]));
        System.out.println("  Black: k" + (!blackRooksMoved[1]) + " q" + (!blackRooksMoved[0]));
        System.out.println("En passant target: " + enPassantTarget);
        System.out.println("FEN: " + toFEN());
    }

    public String toFEN() {
        StringBuilder fen = new StringBuilder();

        // 1. Piece placement
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getFENSymbol());
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append("/");
            }
        }

        // 2. Active color
        fen.append(" ").append(currentPlayer.equals("white") ? "w" : "b");

        // 3. Castling availability
        StringBuilder castling = new StringBuilder();
        if (!whiteKingMoved) {
            if (!whiteRooksMoved[1]) castling.append("K");
            if (!whiteRooksMoved[0]) castling.append("Q");
        }
        if (!blackKingMoved) {
            if (!blackRooksMoved[1]) castling.append("k");
            if (!blackRooksMoved[0]) castling.append("q");
        }
        fen.append(" ").append(castling.length() > 0 ? castling.toString() : "-");

        // 4. En passant target square
        fen.append(" ").append(enPassantTarget != null ? enPassantTarget : "-");

        // 5. Halfmove clock
        fen.append(" ").append(halfMoveClock);

        // 6. Fullmove number
        fen.append(" ").append(fullMoveNumber);

        return fen.toString();
    }


    private boolean hasLegalMoves(String color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board[fromRow][fromCol];
                if (piece != null && piece.getColor().equals(color)) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (piece.isValidMove(toRow, toCol)) {
                                // Simulate move
                                Piece temp = board[toRow][toCol];
                                board[toRow][toCol] = piece;
                                board[fromRow][fromCol] = null;

                                boolean stillInCheck = isKingInCheck(color);

                                // Undo move
                                board[fromRow][fromCol] = piece;
                                board[toRow][toCol] = temp;

                                if (!stillInCheck) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isInsufficientMaterial() {
        // Count all pieces
        int whitePieces = 0;
        int blackPieces = 0;
        boolean whiteHasNonPawn = false;
        boolean blackHasNonPawn = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    if (piece.getColor().equals("white")) {
                        whitePieces++;
                        if (!(piece instanceof Pawn)) whiteHasNonPawn = true;
                    } else {
                        blackPieces++;
                        if (!(piece instanceof Pawn)) blackHasNonPawn = true;
                    }
                }
            }
        }

        // King vs King
        if (whitePieces == 1 && blackPieces == 1) return true;

        // King + minor piece vs King
        if ((whitePieces == 2 && !whiteHasNonPawn && blackPieces == 1) ||
                (blackPieces == 2 && !blackHasNonPawn && whitePieces == 1)) {
            return true;
        }

        return false;
    }

    public boolean isKingInCheck(String color) {
        // Find the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece instanceof King && piece.getColor().equals(color)) {
                    return isSquareUnderAttack(r, c, color.equals("white") ? "black" : "white");
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(String color) {
        if (!isKingInCheck(color)) return false;
        return !hasLegalMoves(color);
    }
}