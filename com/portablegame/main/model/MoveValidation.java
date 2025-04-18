package com.portablegame.main.model;

import com.portablegame.util.MoveCoordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MoveValidation {
    private static final String MOVE_PATTERN =
            "^(?:" +
                    "([KQRBN])([a-h]?[1-8]?)x?([a-h][1-8])(=[QRBN])?" +  // Piece moves
                    "|" +
                    "([a-h])x([a-h][1-8])(=[QRBN])?" +  // Pawn captures
                    "|" +
                    "([a-h][1-8])(=[QRBN])?" +  // Pawn moves
                    "|" +
                    "([KQRBN])([a-h][1-8])" +  // Simple piece moves
                    "|" +
                    "(O-O(?:-O)?)" +  // Castling
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

            // Handle castling moves first
            if (move.equals("O-O") || move.equals("O-O-O")) {
                return validateCastling(move, color, board);
            }

            // Parse algebraic notation
            MoveCoordinates coords = parseAlgebraicNotation(move, color, board);
            if (coords == null) {
                addError("Invalid move notation: %s", move);
                return false;
            }

            return validateStandardMove(move, color, board, coords);
        }

        private MoveCoordinates parseAlgebraicNotation(String move, String color, Board board) {
        // Handle pawn moves
        if (move.matches("[a-h][1-8](?:=[QRBN])?")) {
            return parsePawnMove(move, color, board);
        }
        // Handle piece moves with disambiguation (like Nge2)
        if (move.matches("[KQRBN][a-h]?[1-8]?[a-h][1-8]")) {
            return parsePieceMoveWithDisambiguation(move, color, board);
        }
        // Handle piece captures (like Nxd4)
        if (move.matches("[KQRBN][a-h]?[1-8]?x[a-h][1-8]")) {
            return parsePieceCapture(move, color, board);
        }
        // Handle pawn captures
        if (move.matches("[a-h]x[a-h][1-8](?:=[QRBN])?")) {
            return parsePawnCapture(move, color, board);
        }
        return null;
    }

        private MoveCoordinates parsePieceMoveWithDisambiguation(String move, String color, Board board) {
        char pieceType = move.charAt(0);
        String target = move.substring(move.length() - 2); // Last 2 chars are target square
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';

        // Check for disambiguation (file, rank, or both)
        String disambiguation = move.substring(1, move.length() - 2);

        Class<? extends Piece> pieceClass = getPieceClass(pieceType);

        // Find matching piece that can move to target
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getClass().equals(pieceClass) &&
                        piece.getColor().equals(color)) {

                    // Check disambiguation if present
                    if (!disambiguation.isEmpty()) {
                        // File disambiguation (e.g., Nge2)
                        if (disambiguation.matches("[a-h]")) {
                            int file = disambiguation.charAt(0) - 'a';
                            if (col != file) continue;
                        }
                        // Rank disambiguation (e.g., N1e2)
                        else if (disambiguation.matches("[1-8]")) {
                            int rank = 8 - Integer.parseInt(disambiguation);
                            if (row != rank) continue;
                        }
                    }

                    if (piece.isValidMove(targetRow, targetCol) &&
                            (!(piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) ||
                                    board.isPathClear(row, col, targetRow, targetCol))) {

                        // Check destination
                        Piece targetPiece = board.getPieceAt(targetRow, targetCol);
                        if (targetPiece == null || !targetPiece.getColor().equals(color)) {
                            return new MoveCoordinates(
                                    "" + (char)('a' + col) + (8 - row),
                                    target
                            );
                        }
                    }
                }
            }
        }
        return null;
    }

        private MoveCoordinates parsePawnMove(String move, String color, Board board) {
            String target = move.split("=")[0];
            int targetRow = 8 - Character.getNumericValue(target.charAt(1));
            int targetCol = target.charAt(0) - 'a';
            int direction = color.equals("white") ? -1 : 1;

            // Check single square move
            int sourceRow = targetRow - direction;
            if (sourceRow >= 0 && sourceRow < 8) {
                Piece pawn = board.getPieceAt(sourceRow, targetCol);
                if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                    return new MoveCoordinates(
                            "" + target.charAt(0) + (8 - sourceRow),
                            target
                    );
                }
            }

            // Check double move from starting position
            int startRow = color.equals("white") ? 6 : 1;
            if (targetRow == startRow + 2 * direction) {
                Piece pawn = board.getPieceAt(startRow, targetCol);
                if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                    return new MoveCoordinates(
                            "" + target.charAt(0) + (8 - startRow),
                            target
                    );
                }
            }

            return null;
        }

        private MoveCoordinates parsePawnCapture(String move, String color, Board board) {
            String[] parts = move.split("=");
            String capturePart = parts[0];
            char sourceFile = capturePart.charAt(0);
            String target = capturePart.substring(2);

            int targetRow = 8 - Character.getNumericValue(target.charAt(1));
            int targetCol = target.charAt(0) - 'a';
            int sourceCol = sourceFile - 'a';
            int direction = color.equals("white") ? -1 : 1;
            int sourceRow = targetRow - direction;

            // Check normal capture
            if (sourceRow >= 0 && sourceRow < 8) {
                Piece pawn = board.getPieceAt(sourceRow, sourceCol);
                if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                    return new MoveCoordinates(
                            "" + sourceFile + (8 - sourceRow),
                            target
                    );
                }
            }

            // Check en passant
            String epTarget = board.getEnPassantTarget();
            if (epTarget != null && epTarget.equals(target)) {
                int epRow = color.equals("white") ? 3 : 4;
                Piece pawn = board.getPieceAt(epRow, sourceCol);
                if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                    return new MoveCoordinates(
                            "" + sourceFile + (8 - epRow),
                            target
                    );
                }
            }

            return null;
        }

        private MoveCoordinates parsePieceCapture(String move, String color, Board board) {
        char pieceType = move.charAt(0);
        String target = move.substring(move.indexOf("x") + 1);
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';

        // Get disambiguation info (if any)
        String disambiguation = move.substring(1, move.indexOf("x"));

        Class<? extends Piece> pieceClass = getPieceClass(pieceType);

        // Find matching piece that can capture target
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getClass().equals(pieceClass) &&
                        piece.getColor().equals(color)) {

                    // Check disambiguation if present
                    if (!disambiguation.isEmpty()) {
                        // File disambiguation (e.g., Nfxd4)
                        if (disambiguation.matches("[a-h]")) {
                            int file = disambiguation.charAt(0) - 'a';
                            if (col != file) continue;
                        }
                        // Rank disambiguation (e.g., N3xd4)
                        else if (disambiguation.matches("[1-8]")) {
                            int rank = 8 - Integer.parseInt(disambiguation);
                            if (row != rank) continue;
                        }
                    }

                    if (piece.isValidMove(targetRow, targetCol)) {
                        Piece targetPiece = board.getPieceAt(targetRow, targetCol);
                        if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                            return new MoveCoordinates(
                                    "" + (char)('a' + col) + (8 - row),
                                    target
                            );
                        }
                    }
                }
            }
        }
        return null;
    }

        private Class<? extends Piece> getPieceClass(char pieceChar) {
            switch (Character.toUpperCase(pieceChar)) {
                case 'K': return King.class;
                case 'Q': return Queen.class;
                case 'R': return Rook.class;
                case 'B': return Bishop.class;
                case 'N': return Knight.class;
                default: return Pawn.class;
            }
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
                "e" + (color.equals("white") ? "1" : "8"),
                (kingside ? "g" : "c") + (color.equals("white") ? "1" : "8")
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
        boolean isCapture = move.contains("x") ||
                (piece instanceof Pawn && coords.fromCol != coords.toCol);

        if (isCapture) {
            if (!validateCapture(piece, coords, board)) {
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

    private boolean validateCapture(Piece piece, MoveCoordinates coords, Board board) {
        Piece target = board.getPieceAt(coords.to);

        if (target == null) {
            // For non-pawn pieces, capturing an empty square is invalid
            if (!(piece instanceof Pawn)) {
                addError("No piece to capture at %s", coords.to);
                return false;
            }
            // For pawns, must be valid en passant
            if (!board.isEnPassantPossible(coords.to)) {
                addError("Invalid en passant capture at %s", coords.to);
                return false;
            }
        } else if (target.getColor().equals(piece.getColor())) {
            // Can't capture your own piece
            addError("Cannot capture own %s at %s",
                    target.getClass().getSimpleName(), coords.to);
            return false;
        }
        return true;
    }

    private boolean validatePieceMovement(Piece piece, MoveCoordinates coords, Board board) {
        // Check if the move is valid for the piece
        if (!piece.isValidMove(coords.toRow, coords.toCol)) {
            addError("Illegal %s move from %s to %s",
                    piece.getClass().getSimpleName(), coords.from, coords.to);
            return false;
        }

        // For sliding pieces (Rook, Bishop, Queen), verify path is clear
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            if (!board.isPathClear(piece.getRow(), piece.getCol(), coords.toRow, coords.toCol)) {
                addError("Path blocked for %s from %s to %s",
                        piece.getClass().getSimpleName(), coords.from, coords.to);
                return false;
            }
        }

        // Special pawn rules
        if (piece instanceof Pawn) {
            int direction = piece.getColor().equals("white") ? -1 : 1;

            // Check for diagonal captures
            if (coords.fromCol != coords.toCol) {
                if (!validatePawnCapture((Pawn) piece, coords, board)) {
                    return false;
                }
            } else {
                // Forward move - square must be empty
                if (board.getPieceAt(coords.toRow, coords.toCol) != null) {
                    addError("Pawn cannot move forward to occupied square");
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validatePawnCapture(Pawn pawn, MoveCoordinates coords, Board board) {
        Piece target = board.getPieceAt(coords.to);
        if (target == null && !board.isEnPassantPossible(coords.to)) {
            addError("Invalid pawn capture - no target piece");
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

    private boolean wouldLeaveKingInCheck(Piece piece, MoveCoordinates coords, Board board) {
        Piece captured = board.getPieceAt(coords.toRow, coords.toCol);

        // Simulate move
        board.setPieceAt(coords.toRow, coords.toCol, piece);
        board.setPieceAt(piece.getRow(), piece.getCol(), null);

        boolean inCheck = board.isKingInCheck(piece.getColor());

        // Undo simulation
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

    public void addError(String error) {
        errors.add(String.format("Move %d (%s): %s", moveNumber, color, error));
        isValid = false;
    }

    public void addError(String format, Object... args) {
        addError(String.format(format, args));
    }

    public boolean isValid() { return isValid && errors.isEmpty(); }
    public MoveCoordinates getCoordinates() { return coordinates; }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public String getPromotionPiece() { return promotionPiece; }
    public String getOriginalNotation() { return originalNotation; }
    public int getMoveNumber() { return moveNumber; }
    public String getColor() { return color; }
}