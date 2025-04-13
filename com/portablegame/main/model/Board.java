package com.portablegame.main.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] board;
    private String enPassantTarget;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean[] whiteRooksMoved = {false, false}; // [queenside, kingside]
    private boolean[] blackRooksMoved = {false, false};
    private List<String> moveHistory = new ArrayList<>();
    private String currentPlayer = "white";

    public Board() {
        this.board = new Piece[8][8];
        initializeBoard();
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

    public boolean tryMove(String from, String to, String promotionChoice) {
        if (from == null || to == null || !from.matches("[a-h][1-8]") || !to.matches("[a-h][1-8]")) {
            return false;
        }

        Piece piece = getPieceAt(from);
        if (piece == null || !piece.getColor().equals(currentPlayer)) {
            return false;
        }

        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
        int fromCol = from.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));
        int toCol = to.charAt(0) - 'a';

        // Handle castling
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            return tryCastle(fromRow, fromCol, toCol);
        }

        // Validate move
        if (!piece.isValidMove(toRow, toCol)) {
            return false;
        }

        // Handle en passant
        if (piece instanceof Pawn && to.equals(enPassantTarget) && Math.abs(fromCol - toCol) == 1) {
            executeEnPassant(fromRow, fromCol, toRow, toCol);
            return true;
        }

        // Check path is clear (for sliding pieces)
        if ((piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) &&
                !isPathClear(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Execute move
        executeMove(fromRow, fromCol, toRow, toCol, promotionChoice);
        return true;
    }

    private void executeMove(int fromRow, int fromCol, int toRow, int toCol, String promotionChoice) {
        Piece piece = board[fromRow][fromCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setPosition(toRow, toCol);

        // Handle pawn promotion
        if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
            promotePawn(toRow, toCol, promotionChoice);
        }

        // Set en passant target if pawn moves two squares
        if (piece instanceof Pawn && Math.abs(fromRow - toRow) == 2) {
            int enPassantRow = piece.getColor().equals("white") ? toRow + 1 : toRow - 1;
            enPassantTarget = "" + (char)('a' + toCol) + (8 - enPassantRow);
        } else {
            enPassantTarget = null;
        }

        // Update castling rights
        updateCastlingStatus(piece, fromRow, fromCol);

        // Record move and switch player
        moveHistory.add(currentPlayer + ": " + positionToNotation(fromRow, fromCol) +
                "-" + positionToNotation(toRow, toCol));
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    private void executeEnPassant(int fromRow, int fromCol, int toRow, int toCol) {
        Piece pawn = board[fromRow][fromCol];
        board[toRow][toCol] = pawn;
        board[fromRow][fromCol] = null;

        // Remove the captured pawn
        int capturedPawnRow = fromRow; // Same row as moving pawn
        int capturedPawnCol = toCol;    // Different column
        board[capturedPawnRow][capturedPawnCol] = null;

        pawn.setPosition(toRow, toCol);
        enPassantTarget = null;
        moveHistory.add(currentPlayer + ": " + positionToNotation(fromRow, fromCol) +
                "x" + positionToNotation(toRow, toCol) + " e.p.");
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    private boolean tryCastle(int row, int kingCol, int targetCol) {
        boolean kingside = targetCol > kingCol;
        int rookCol = kingside ? 7 : 0;

        // Check if pieces are in correct positions
        Piece king = board[row][kingCol];
        Piece rook = board[row][rookCol];
        if (!(king instanceof King) || !(rook instanceof Rook)) {
            return false;
        }

        // Check if pieces haven't moved
        if (king.getColor().equals("white")) {
            if (whiteKingMoved || (kingside ? whiteRooksMoved[1] : whiteRooksMoved[0])) {
                return false;
            }
        } else {
            if (blackKingMoved || (kingside ? blackRooksMoved[1] : blackRooksMoved[0])) {
                return false;
            }
        }

        // Check path is clear
        int step = kingside ? 1 : -1;
        for (int col = kingCol + step; col != rookCol; col += step) {
            if (board[row][col] != null) {
                return false;
            }
        }

        // Execute castling
        int newKingCol = kingCol + 2 * step;
        int newRookCol = kingCol + step;

        board[row][newKingCol] = king;
        board[row][newRookCol] = rook;
        board[row][kingCol] = null;
        board[row][rookCol] = null;

        king.setPosition(row, newKingCol);
        rook.setPosition(row, newRookCol);

        moveHistory.add(currentPlayer + ": " + (kingside ? "O-O" : "O-O-O"));
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
        return true;
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

    protected boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
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

    // Getters for game state
    public String getEnPassantTarget() { return enPassantTarget; }
    public String getCurrentPlayer() { return currentPlayer; }
    public List<String> getMoveHistory() { return new ArrayList<>(moveHistory); }



    // In Board.java
    public boolean isSquareUnderAttack(int row, int col, String attackerColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getColor().equals(attackerColor)) {
                    if (piece.isValidMove(row, col)) {
                        return true;
                    }
                }
            }
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

        // Try every possible move for this color
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
                                    return false; // Found a legal move that gets out of check
                                }
                            }
                        }
                    }
                }
            }
        }
        return true; // No legal moves found
    }
}