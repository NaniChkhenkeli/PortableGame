package com.portablegame.main.model;

import com.portablegame.util.MoveCoordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoveValidation {
    private static final String MOVE_PATTERN =
            "^(?:" +
                    "([KQRBN])([a-h]?[1-8]?)x?([a-h][1-8])(=[QRBN])?" +
                    "|" +
                    "([a-h])x([a-h][1-8])(=[QRBN])?" +
                    "|" +
                    "([a-h][1-8])(=[QRBN])?" +
                    "|" +
                    "(O-O(?:-O)?)" +
                    ")" +
                    "[+#]?$";


    private final int moveNumber;
    private final String originalNotation;
    private final String color;
    private MoveCoordinates coordinates;
    private boolean isValid;
    private final List<String> errors = new ArrayList<>();
    private String promotionPiece;

    public MoveValidation(int moveNumber, String notation, String color) {
        this.moveNumber = Objects.requireNonNull(moveNumber);
        this.originalNotation = Objects.requireNonNull(notation);
        this.color = Objects.requireNonNull(color);
        this.isValid = true;
    }

    public boolean validateMove(String move, String color, Board board) {
        resetValidationState();

        // Basic syntax validation
        if (!isValidSyntax(move)) {
            return false;
        }

        // Handle castling moves
        if (move.startsWith("O-O")) {
            return validateCastling(move, color, board);
        }

        // Parse and validate standard moves
        MoveCoordinates coords = MoveCoordinates.fromAlgebraic(move, color, board);
        if (coords == null) {
            addError("Invalid move notation: %s", move);
            return false;
        }

        return validateStandardMove(move, color, board, coords);
    }

    private boolean isValidSyntax(String move) {
        if (!move.matches(MOVE_PATTERN)) {
            addError("Malformed move syntax: %s", move);
            return false;
        }
        return true;
    }

    private boolean validateCastling(String move, String color, Board board) {
        boolean kingside = move.equals("O-O");
        int row = color.equals("white") ? 7 : 0;
        int kingCol = 4;
        int kingDestCol = kingside ? 6 : 2;

        // Verify king exists and hasn't moved
        Piece king = board.getPieceAt(row, kingCol);
        if (!(king instanceof King) || !king.getColor().equals(color)) {
            addError("King not in starting position");
            return false;
        }

        // Check if king is currently in check
        if (board.isSquareUnderAttack(row, kingCol, color.equals("white") ? "black" : "white")) {
            addError("Cannot castle while in check");
            return false;
        }

        // Check squares king moves through aren't under attack
        int step = kingside ? 1 : -1;
        for (int col = kingCol; col != kingDestCol; col += step) {
            if (col != kingCol && board.isSquareUnderAttack(row, col, color.equals("white") ? "black" : "white")) {
                addError("Cannot castle through check");
                return false;
            }
        }

        this.coordinates = new MoveCoordinates(
                "e" + (row + 1),
                (kingside ? "g" : "c") + (row + 1)
        );
        return true;
    }


    private boolean validateStandardMove(String move, String color, Board board,
                                         MoveCoordinates coords) {
        // Get the moving piece
        Piece piece = board.getPieceAt(coords.from);

        // Verify piece exists and is correct color
        if (piece == null) {
            addError("No piece at %s", coords.from);
            return false;
        }
        if (!piece.getColor().equals(color)) {
            addError("Wrong color piece at %s", coords.from);
            return false;
        }

        // Validate piece-specific movement rules
        if (!validatePieceMovement(piece, coords, board)) {
            return false;
        }

        // Check for promotion
        if (move.contains("=")) {
            if (!validatePromotion(piece, coords.toRow, move)) {
                return false;
            }
        }

        // Handle capture moves
        if (move.contains("x")) {
            Piece target = board.getPieceAt(coords.to);

            if (target == null) {
                // For non-pawn pieces, capturing empty square is invalid
                if (!(piece instanceof Pawn)) {
                    addError("No piece to capture at %s", coords.to);
                    return false;
                }
                // For pawns, must be valid en passant
                if (!validateEnPassant(piece, coords, board)) {
                    addError("Invalid en passant capture at %s", coords.to);
                    return false;
                }
            } else if (target.getColor().equals(color)) {
                // Can't capture your own piece
                addError("Cannot capture own %s at %s",
                        target.getClass().getSimpleName(), coords.to);
                return false;
            }
        } else {
            // For non-capture moves, destination must be empty
            if (board.getPieceAt(coords.to) != null) {
                addError("%s is blocked by %s", coords.to,
                        board.getPieceAt(coords.to).getClass().getSimpleName());
                return false;
            }
        }

        // Verify not leaving king in check
        if (wouldLeaveKingInCheck(piece, coords, board)) {
            addError("Move would leave king in check");
            return false;
        }

        this.coordinates = coords;
        return true;
    }

    private boolean validatePieceMovement(Piece piece, MoveCoordinates coords, Board board) {
        if (!piece.isValidMove(coords.toRow, coords.toCol)) {
            addError("Illegal %s move from %s to %s",
                    piece.getClass().getSimpleName(), coords.from, coords.to);
            return false;
        }

        // For sliding pieces, verify path is clear
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            if (!board.isPathClear(piece.getRow(), piece.getCol(),
                    coords.toRow, coords.toCol)) {
                addError("Path blocked for %s from %s to %s",
                        piece.getClass().getSimpleName(), coords.from, coords.to);
                return false;
            }
        }

        // Verify capture rules
        Piece target = board.getPieceAt(coords.to);
        if (target != null && target.getColor().equals(color)) {
            addError("Cannot capture own piece at %s", coords.to);
            return false;
        }

        return true;
    }

    private boolean validatePromotion(Piece piece, int toRow, String move) {
        if (!(piece instanceof Pawn)) {
            addError("Only pawns can promote");
            return false;
        }

        int promotionRow = piece.getColor().equals("white") ? 0 : 7;
        if (toRow != promotionRow) {
            addError("Promotion only on last rank");
            return false;
        }

        String promoPiece = move.substring(move.indexOf("=") + 1, move.indexOf("=") + 2);
        if (!"QRBN".contains(promoPiece.toUpperCase())) {
            addError("Invalid promotion piece: %s", promoPiece);
            return false;
        }

        this.promotionPiece = promoPiece;
        return true;
    }

    private boolean validateEnPassant(Piece piece, MoveCoordinates coords, Board board) {
        if (!(piece instanceof Pawn)) {
            addError("Only pawns can capture en passant");
            return false;
        }

        String epTarget = board.getEnPassantTarget();
        if (epTarget == null || !epTarget.equals(coords.to)) {
            addError("No en passant target at %s", coords.to);
            return false;
        }

        int direction = piece.getColor().equals("white") ? -1 : 1;
        if (coords.toRow != piece.getRow() + direction) {
            addError("Invalid en passant capture direction");
            return false;
        }

        return true;
    }

    private boolean wouldLeaveKingInCheck(Piece piece, MoveCoordinates coords, Board board) {
        Piece captured = board.getPieceAt(coords.toRow, coords.toCol);

        board.setPieceAt(coords.toRow, coords.toCol, piece);
        board.setPieceAt(piece.getRow(), piece.getCol(), null);

        boolean inCheck = board.isKingInCheck(piece.getColor());

        board.setPieceAt(piece.getRow(), piece.getCol(), piece);
        board.setPieceAt(coords.toRow, coords.toCol, captured);

        return inCheck;
    }

    private void resetValidationState() {
        this.isValid = true;
        this.errors.clear();
        this.coordinates = null;
        this.promotionPiece = null;
    }

    // Error handling methods
    public void addError(String error) {
        errors.add(String.format("Move %d (%s): %s", moveNumber, color, error));
        isValid = false;
    }

    public void addError(String format, Object... args) {
        addError(String.format(format, args));
    }

    // Getters and setters
    public boolean isValid() { return isValid && errors.isEmpty(); }
    public MoveCoordinates getCoordinates() { return coordinates; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public String getPromotionPiece() { return promotionPiece; }
    public String getOriginalNotation() { return originalNotation; }
    public int getMoveNumber() { return moveNumber; }
    public String getColor() { return color; }
}