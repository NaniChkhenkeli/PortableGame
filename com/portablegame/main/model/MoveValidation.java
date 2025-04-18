package com.portablegame.main.model;

import com.portablegame.util.MoveCoordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveValidation {
    private static final String MOVE_PATTERN =
            "^(?:" +
                    "([KQRBN])([a-h]?[1-8]?)x?([a-h][1-8])(=[QRBN])?[+#]?" +  // piece moves/captures
                    "|" +
                    "([a-h])x([a-h][1-8])(=[QRBN])?[+#]?" +  // pawn captures
                    "|" +
                    "([a-h][1-8])(=[QRBN])?[+#]?" +  // pawn moves
                    "|" +
                    "(O-O(?:-O)?)[+#]?" +  // castling
                    ")$";

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

        // Basic syntax check
        if (!isValidSyntax(move)) {
            return false;
        }

        // Parse the move
        MoveCoordinates coords = parseAlgebraicNotation(move, color, board);
        if (coords == null) {
            addError("Could not parse move: " + move);
            return false;
        }

        // Validate piece movement rules
        if (!validateStandardMove(move, color, board, coords)) {
            return false;
        }

        // Validate check indicators if present
        if (!validateCheckIndicators(move, color, board, coords)) {
            return false;
        }

        this.coordinates = coords;
        return true;
    }

    private MoveCoordinates parseAlgebraicNotation(String move, String color, Board board) {
        // Remove check/mate symbols and annotations first
        String cleanMove = move.replaceAll("[+#!?]", "").trim();

        try {
            // Handle castling
            if (cleanMove.equals("O-O") || cleanMove.equals("O-O-O")) {
                return parseCastlingMove(cleanMove, color, board);
            }

            // Handle pawn promotions
            if (cleanMove.matches("[a-h][18]=[QRBN]")) {
                return parsePawnPromotion(cleanMove, color, board);
            }

            // Handle piece moves (with optional disambiguation)
            Matcher m = Pattern.compile("^([KQRBN])([a-h]?[1-8]?)(x?)([a-h][1-8])(=[QRBN])?$").matcher(cleanMove);
            if (m.matches()) {
                return parsePieceMove(m.group(1).charAt(0), m.group(2),
                        m.group(3).equals("x"), m.group(4), color, board);
            }

            // Handle pawn captures
            m = Pattern.compile("^([a-h])x([a-h][1-8])(=[QRBN])?$").matcher(cleanMove);
            if (m.matches()) {
                return parsePawnCapture(m.group(1).charAt(0), m.group(2),
                        m.group(3) != null ? m.group(3).substring(1) : null, color, board);
            }

            // Handle simple pawn moves
            if (cleanMove.matches("^[a-h][1-8]$")) {
                return parsePawnMove(cleanMove, color, board);
            }

            // Handle queen moves specifically
            if (cleanMove.matches("^Q[a-h]?[1-8]?[a-h][1-8]$")) {
                return parseQueenMove(cleanMove, color, board);
            }
        } catch (Exception e) {
            addError("Error parsing move '" + move + "': " + e.getMessage());
        }

        return null;
    }

    private MoveCoordinates parseQueenMove(String move, String color, Board board) {
        char pieceType = 'Q';
        String target = move.substring(move.length()-2);
        String disambiguation = move.substring(1, move.length()-2);
        return findPieceMove(pieceType, target, disambiguation, color, board, move.contains("x"));
    }

    private MoveCoordinates parsePawnPromotion(String move, String color, Board board) {
        String target = move.substring(0, 2);
        String promoPiece = move.substring(3);
        MoveCoordinates coords = parsePawnMove(target, color, board);
        if (coords != null) {
            this.promotionPiece = promoPiece;
        }
        return coords;
    }

    private MoveCoordinates parsePieceMove(char pieceType, String disambiguation,
                                           boolean isCapture, String target,
                                           String color, Board board) {
        return findPieceMove(pieceType, target, disambiguation, color, board, isCapture);
    }

    private MoveCoordinates findPieceMove(char pieceType, String target,
                                          String disambiguation, String color,
                                          Board board, boolean isCapture) {
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';

        Class<? extends Piece> pieceClass = getPieceClass(pieceType);

        // Find matching piece that can move to target
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getClass().equals(pieceClass) &&
                        piece.getColor().equals(color)) {

                    // Check disambiguation if present
                    if (!disambiguation.isEmpty()) {
                        if (disambiguation.matches("[a-h]")) { // File disambiguation
                            int file = disambiguation.charAt(0) - 'a';
                            if (col != file) continue;
                        } else if (disambiguation.matches("[1-8]")) { // Rank disambiguation
                            int rank = 8 - Integer.parseInt(disambiguation);
                            if (row != rank) continue;
                        }
                    }

                    if (piece.isValidMove(targetRow, targetCol)) {
                        Piece targetPiece = board.getPieceAt(targetRow, targetCol);
                        if (isCapture) {
                            if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                                return new MoveCoordinates(
                                        "" + (char)('a' + col) + (8 - row),
                                        target
                                );
                            }
                        } else if (targetPiece == null) {
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

    private MoveCoordinates parsePawnCapture(char sourceFile, String target,
                                             String promotion, String color,
                                             Board board) {
        int targetRow = 8 - Character.getNumericValue(target.charAt(1));
        int targetCol = target.charAt(0) - 'a';
        int sourceCol = sourceFile - 'a';
        int direction = color.equals("white") ? -1 : 1;
        int sourceRow = targetRow - direction;

        // Check normal capture
        if (sourceRow >= 0 && sourceRow < 8) {
            Piece pawn = board.getPieceAt(sourceRow, sourceCol);
            if (pawn instanceof Pawn && pawn.getColor().equals(color)) {
                if (promotion != null) {
                    this.promotionPiece = promotion;
                }
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
            addError("Malformed move syntax: " + move);
            return false;
        }
        return true;
    }

    private boolean validateStandardMove(String move, String color, Board board,
                                         MoveCoordinates coords) {
        // Get the moving piece
        Piece piece = board.getPieceAt(coords.fromRow, coords.fromCol);

        // Verify piece exists and is correct color
        if (piece == null) {
            addError("No piece at " + coords.from);
            return false;
        }
        if (!piece.getColor().equals(color)) {
            addError("Wrong color piece at " + coords.from);
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
            if (board.getPieceAt(coords.toRow, coords.toCol) != null) {
                addError(coords.to + " is blocked");
                return false;
            }
        }

        if (wouldLeaveKingInCheck(piece, coords, board)) {
            addError("Move would leave king in check");
            return false;
        }

        return true;
    }

    private boolean validateCheckIndicators(String move, String color, Board board, MoveCoordinates coords) {
        if (move.endsWith("+") || move.endsWith("#")) {
            Piece movingPiece = board.getPieceAt(coords.fromRow, coords.fromCol);
            Piece captured = board.getPieceAt(coords.toRow, coords.toCol);
            board.setPieceAt(coords.toRow, coords.toCol, movingPiece);
            board.setPieceAt(coords.fromRow, coords.fromCol, null);
            boolean isCheck = board.isKingInCheck(color.equals("white") ? "black" : "white");
            board.setPieceAt(coords.fromRow, coords.fromCol, movingPiece);
            board.setPieceAt(coords.toRow, coords.toCol, captured);

            if (!isCheck) {
                addError("Move claims check but doesn't put opponent in check");
                return false;
            }
        }
        return true;
    }


    private boolean causesCheck(Board board, MoveCoordinates coords, String movingColor) {
        Piece movingPiece = board.getPieceAt(coords.fromRow, coords.fromCol);
        Piece captured = board.getPieceAt(coords.toRow, coords.toCol);

        board.setPieceAt(coords.toRow, coords.toCol, movingPiece);
        board.setPieceAt(coords.fromRow, coords.fromCol, null);

        boolean isCheck = board.isKingInCheck(movingColor.equals("white") ? "black" : "white");

        board.setPieceAt(coords.fromRow, coords.fromCol, movingPiece);
        board.setPieceAt(coords.toRow, coords.toCol, captured);

        return isCheck;
    }

    private boolean validateCastling(String move, String color, Board board) {
        boolean kingside = move.equals("O-O");
        int row = color.equals("white") ? 7 : 0;
        int kingCol = 4;
        int kingDestCol = kingside ? 6 : 2;

        Piece king = board.getPieceAt(row, kingCol);
        if (!(king instanceof King) || !king.getColor().equals(color)) {
            addError("King not in starting position");
            return false;
        }

        if (board.isSquareUnderAttack(row, kingCol, color.equals("white") ? "black" : "white")) {
            addError("Cannot castle while in check");
            return false;
        }

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

    private boolean validatePieceMovement(Piece piece, MoveCoordinates coords, Board board) {
        if (!piece.isValidMove(coords.toRow, coords.toCol)) {
            addError("Illegal " + piece.getClass().getSimpleName() + " move");
            return false;
        }

        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            if (!board.isPathClear(piece.getRow(), piece.getCol(), coords.toRow, coords.toCol)) {
                addError("Path blocked for " + piece.getClass().getSimpleName());
                return false;
            }
        }

        if (piece instanceof Pawn) {
            if (coords.fromCol != coords.toCol) {
                if (!validatePawnCapture((Pawn) piece, coords, board)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validatePawnCapture(Pawn pawn, MoveCoordinates coords, Board board) {
        Piece target = board.getPieceAt(coords.toRow, coords.toCol);
        if (target == null && !board.isEnPassantPossible(coords.to)) {
            addError("Invalid pawn capture");
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

        if (move.contains("=")) {
            String promoPiece = move.substring(move.indexOf("=") + 1).replaceAll("[+#]", "");
            if (!"QRBN".contains(promoPiece.toUpperCase())) {
                addError("Invalid promotion piece: " + promoPiece);
                return false;
            }
            this.promotionPiece = promoPiece;
        } else {
            this.promotionPiece = "Q";
        }
        return true;
    }

    private boolean validateCapture(Piece piece, MoveCoordinates coords, Board board) {
        Piece target = board.getPieceAt(coords.toRow, coords.toCol);

        if (target == null) {
            if (!(piece instanceof Pawn)) {
                addError("No piece to capture at " + coords.to);
                return false;
            }
            if (!board.isEnPassantPossible(coords.to)) {
                addError("Invalid en passant capture");
                return false;
            }
        } else if (target.getColor().equals(piece.getColor())) {
            addError("Cannot capture own piece");
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

    private MoveCoordinates parseCastlingMove(String move, String color, Board board) {
        boolean kingside = move.equals("O-O");
        return new MoveCoordinates(
                "e" + (color.equals("white") ? "1" : "8"),
                (kingside ? "g" : "c") + (color.equals("white") ? "1" : "8")
        );
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