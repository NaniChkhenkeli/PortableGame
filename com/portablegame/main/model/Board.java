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
    private boolean[] whiteRooksMoved = {false, false};
    private boolean[] blackRooksMoved = {false, false};
    private List<String> moveHistory = new ArrayList<>();
    private String currentPlayer = "white";
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    private Map<String, Integer> positionCounts = new HashMap<>();
    private boolean whiteResigned = false;
    private boolean blackResigned = false;

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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }

        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("black", 1, i, this);
            board[6][i] = new Pawn("white", 6, i, this);
        }

        board[0][0] = new Rook("black", 0, 0, this);
        board[0][1] = new Knight("black", 0, 1, this);
        board[0][2] = new Bishop("black", 0, 2, this);
        board[0][3] = new Queen("black", 0, 3, this);
        board[0][4] = new King("black", 0, 4, this);
        board[0][5] = new Bishop("black", 0, 5, this);
        board[0][6] = new Knight("black", 0, 6, this);
        board[0][7] = new Rook("black", 0, 7, this);

        board[7][0] = new Rook("white", 7, 0, this);
        board[7][1] = new Knight("white", 7, 1, this);
        board[7][2] = new Bishop("white", 7, 2, this);
        board[7][3] = new Queen("white", 7, 3, this);
        board[7][4] = new King("white", 7, 4, this);
        board[7][5] = new Bishop("white", 7, 5, this);
        board[7][6] = new Knight("white", 7, 6, this);
        board[7][7] = new Rook("white", 7, 7, this);

        enPassantTarget = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRooksMoved = new boolean[]{false, false};
        blackRooksMoved = new boolean[]{false, false};
        whiteResigned = false;
        blackResigned = false;
        moveHistory.clear();
        currentPlayer = "white";
        halfMoveClock = 0;
        fullMoveNumber = 1;
        positionCounts.clear();
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
        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
        int fromCol = from.charAt(0) - 'a';
        int toRow = 8 - Character.getNumericValue(to.charAt(1));
        int toCol = to.charAt(0) - 'a';

        Piece piece = getPieceAt(fromRow, fromCol);
        if (piece == null || !piece.getColor().equals(currentPlayer)) {
            return false;
        }

        if (piece instanceof King && Math.abs(fromCol - toCol) == 2 && fromRow == toRow) {
            return tryCastle(currentPlayer, toCol > fromCol);
        }
        if (!piece.isValidMove(toRow, toCol)) {
            return false;
        }

        if (piece instanceof Pawn && isEnPassantPossible(to)) {
            executeEnPassant(fromRow, fromCol, toRow, toCol);
            return true;
        }

        if (piece instanceof Pawn) {
            if ((piece.getColor().equals("white") && toRow == 7)) {
                if (promotionChoice == null) promotionChoice = "Q";
                promotePawn(toRow, toCol, promotionChoice);
                executeMove(fromRow, fromCol, toRow, toCol, null);
                return true;
            }
            if ((piece.getColor().equals("black") && toRow == 0)) {
                if (promotionChoice == null) promotionChoice = "Q";
                promotePawn(toRow, toCol, promotionChoice);
                executeMove(fromRow, fromCol, toRow, toCol, null);
                return true;
            }
        }

        Piece targetPiece = getPieceAt(toRow, toCol);
        if (!(piece instanceof Pawn) && targetPiece != null &&
                targetPiece.getColor().equals(currentPlayer)) {
            return false;
        }

        if (!(piece instanceof Knight) &&
                (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) &&
                !isPathClear(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        if (wouldLeaveKingInCheck(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        executeMove(fromRow, fromCol, toRow, toCol, null);
        return true;
    }

    private boolean wouldLeaveKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece movingPiece = board[fromRow][fromCol];
        Piece capturedPiece = board[toRow][toCol];

        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
        movingPiece.setPosition(toRow, toCol);

        boolean inCheck = isKingInCheck(movingPiece.getColor());

        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        movingPiece.setPosition(fromRow, fromCol);

        return inCheck;
    }

    private void executeMove(int fromRow, int fromCol, int toRow, int toCol, String promotionChoice) {
        Piece piece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];

        updateCastlingStatus(piece, fromRow, fromCol);

        if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
            promotePawn(toRow, toCol, promotionChoice);
            piece = board[toRow][toCol];
        } else {
            board[toRow][toCol] = piece;
            piece.setPosition(toRow, toCol);
        }

        if (captured != null || piece instanceof Pawn) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if (piece instanceof Pawn && Math.abs(fromRow - toRow) == 2) {
            int epRow = piece.getColor().equals("white") ? toRow + 1 : toRow - 1;
            enPassantTarget = "" + (char)('a' + toCol) + (8 - epRow);
        } else {
            enPassantTarget = null;
        }

        if (currentPlayer.equals("black")) {
            fullMoveNumber++;
        }

        String fen = toFEN();
        positionCounts.put(fen, positionCounts.getOrDefault(fen, 0) + 1);

        moveHistory.add(currentPlayer + ": " + positionToNotation(fromRow, fromCol) +
                (captured != null ? "x" : "-") + positionToNotation(toRow, toCol));
        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
    }

    public King getKing(String color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece instanceof King && piece.getColor().equals(color)) {
                    return (King) piece;
                }
            }
        }
        return null;
    }

    public boolean isEnPassantPossible(String targetSquare) {
        if (enPassantTarget == null || !enPassantTarget.equals(targetSquare)) {
            return false;
        }

        int row = 8 - Character.getNumericValue(targetSquare.charAt(1));
        int col = targetSquare.charAt(0) - 'a';

        if (currentPlayer.equals("white") && row == 2) {
            Piece leftPawn = col > 0 ? getPieceAt(3, col-1) : null;
            Piece rightPawn = col < 7 ? getPieceAt(3, col+1) : null;
            return (leftPawn instanceof Pawn && leftPawn.getColor().equals("white")) ||
                    (rightPawn instanceof Pawn && rightPawn.getColor().equals("white"));
        }

        if (currentPlayer.equals("black") && row == 5) {
            Piece leftPawn = col > 0 ? getPieceAt(4, col-1) : null;
            Piece rightPawn = col < 7 ? getPieceAt(4, col+1) : null;
            return (leftPawn instanceof Pawn && leftPawn.getColor().equals("black")) ||
                    (rightPawn instanceof Pawn && rightPawn.getColor().equals("black"));
        }

        return false;
    }

    private void executeEnPassant(int fromRow, int fromCol, int toRow, int toCol) {
        Piece pawn = board[fromRow][fromCol];
        board[toRow][toCol] = pawn;
        board[fromRow][fromCol] = null;

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

        Piece king = getPieceAt(row, kingCol);
        if (!(king instanceof King) || !king.getColor().equals(color)) {
            return false;
        }

        Piece rook = getPieceAt(row, rookCol);
        if (!(rook instanceof Rook) || !rook.getColor().equals(color)) {
            return false;
        }

        int step = kingside ? 1 : -1;
        for (int col = kingCol + step; col != rookCol; col += step) {
            if (getPieceAt(row, col) != null) {
                return false;
            }
        }

        if (isSquareUnderAttack(row, kingCol, color.equals("white") ? "black" : "white")) {
            return false;
        }

        for (int col = kingCol; col != kingDestCol; col += step) {
            if (col != kingCol && isSquareUnderAttack(row, col, color.equals("white") ? "black" : "white")) {
                return false;
            }
        }

        if (isSquareUnderAttack(row, kingDestCol, color.equals("white") ? "black" : "white")) {
            return false;
        }

        performCastling(row, kingCol, rookCol, kingDestCol, rookDestCol, color, kingside);
        return true;
    }

    private void performCastling(int row, int kingCol, int rookCol,
                                 int kingDestCol, int rookDestCol,
                                 String color, boolean kingside) {
        Piece rook = getPieceAt(row, rookCol);
        board[row][rookDestCol] = rook;
        board[row][rookCol] = null;
        rook.setPosition(row, rookDestCol);
        if (rook instanceof Rook) {
            ((Rook)rook).setHasMoved(true);
        }

        Piece king = getPieceAt(row, kingCol);
        board[row][kingDestCol] = king;
        board[row][kingCol] = null;
        king.setPosition(row, kingDestCol);
        if (king instanceof King) {
            ((King)king).setHasMoved(true);
        }

        if (color.equals("white")) {
            whiteKingMoved = true;
            if (kingside) whiteRooksMoved[1] = true;
            else whiteRooksMoved[0] = true;
        } else {
            blackKingMoved = true;
            if (kingside) blackRooksMoved[1] = true;
            else blackRooksMoved[0] = true;
        }

        currentPlayer = currentPlayer.equals("white") ? "black" : "white";
        moveHistory.add(color + ": " + (kingside ? "O-O" : "O-O-O"));
    }

    public void promotePawn(int row, int col, String promotionChoice) {
        String color = board[row][col].getColor();
        board[row][col] = switch (promotionChoice != null ? promotionChoice.toUpperCase() : "Q") {
            case "R" -> new Rook(color, row, col, this);
            case "N" -> new Knight(color, row, col, this);
            case "B" -> new Bishop(color, row, col, this);
            default -> new Queen(color, row, col, this);
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
                if (row == 7 && col == 0) whiteRooksMoved[0] = true;
                if (row == 7 && col == 7) whiteRooksMoved[1] = true;
            } else {
                if (row == 0 && col == 0) blackRooksMoved[0] = true;
                if (row == 0 && col == 7) blackRooksMoved[1] = true;
            }
        }
    }

    public boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
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

    public boolean isSquareUnderAttack(int row, int col, String opponentColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = getPieceAt(r, c);
                if (piece != null && piece.getColor().equals(opponentColor) && piece.isValidMove(row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDraw() {
        if (!isKingInCheck(currentPlayer) && !hasLegalMoves(currentPlayer)) {
            return true;
        }

        if (isInsufficientMaterial()) {
            return true;
        }

        if (halfMoveClock >= 50) {
            return true;
        }

        if (isThreefoldRepetition()) {
            return true;
        }

        if (isDeadPosition()) {
            return true;
        }

        return false;
    }

    public boolean isThreefoldRepetition() {
        String currentPosition = toFEN();

        int count = 0;
        for (int i = moveHistory.size() - 1; i >= 0; i--) {
            String move = moveHistory.get(i);
            if (move.contains(":")) {
                String position = move.split(":")[1].trim();
                if (position.equals(currentPosition)) {
                    count++;
                    if (count >= 3) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }

        for (int i = moveHistory.size() - 1; i >= 3; i--) {
            String move1 = moveHistory.get(i);
            String move2 = moveHistory.get(i - 1);
            String move3 = moveHistory.get(i - 2);
            if (move1.contains(":") && move2.contains(":") && move3.contains(":")) {
                String position1 = move1.split(":")[1].trim();
                String position2 = move2.split(":")[1].trim();
                String position3 = move3.split(":")[1].trim();
                if (position1.equals(currentPosition) && position2.equals(currentPosition) && position3.equals(currentPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isDeadPosition() {
        Map<String, Integer> pieceCount = new HashMap<>();
        pieceCount.put("white", 0);
        pieceCount.put("black", 0);

        boolean whiteHasBishopOrKnight = false;
        boolean blackHasBishopOrKnight = false;
        boolean whiteHasNonKing = false;
        boolean blackHasNonKing = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    String color = piece.getColor();
                    pieceCount.put(color, pieceCount.get(color) + 1);

                    if (!(piece instanceof King)) {
                        if (color.equals("white")) {
                            whiteHasNonKing = true;
                            if (piece instanceof Bishop || piece instanceof Knight) {
                                whiteHasBishopOrKnight = true;
                            }
                        } else {
                            blackHasNonKing = true;
                            if (piece instanceof Bishop || piece instanceof Knight) {
                                blackHasBishopOrKnight = true;
                            }
                        }
                    }
                }
            }
        }

        if (pieceCount.get("white") == 1 && pieceCount.get("black") == 1) {
            return true;
        }

        if ((pieceCount.get("white") == 2 && whiteHasBishopOrKnight && pieceCount.get("black") == 1) ||
                (pieceCount.get("black") == 2 && blackHasBishopOrKnight && pieceCount.get("white") == 1)) {
            return true;
        }

        if (pieceCount.get("white") == 2 && pieceCount.get("black") == 2 &&
                whiteHasBishopOrKnight && blackHasBishopOrKnight) {

            Bishop whiteBishop = null;
            Bishop blackBishop = null;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board[row][col];
                    if (piece instanceof Bishop) {
                        if (piece.getColor().equals("white")) {
                            whiteBishop = (Bishop) piece;
                        } else {
                            blackBishop = (Bishop) piece;
                        }
                    }
                }
            }

            if (whiteBishop != null && blackBishop != null) {
                boolean whiteBishopOnWhite = (whiteBishop.getRow() + whiteBishop.getCol()) % 2 == 0;
                boolean blackBishopOnWhite = (blackBishop.getRow() + blackBishop.getCol()) % 2 == 0;

                if (whiteBishopOnWhite == blackBishopOnWhite) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasLegalMoves(String color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board[fromRow][fromCol];
                if (piece != null && piece.getColor().equals(color)) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (piece.isValidMove(toRow, toCol)) {
                                if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
                                    if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
                                        continue;
                                    }
                                }

                                Piece target = board[toRow][toCol];

                                board[toRow][toCol] = piece;
                                board[fromRow][fromCol] = null;
                                piece.setPosition(toRow, toCol);

                                boolean isLegal = !isKingInCheck(color);

                                board[fromRow][fromCol] = piece;
                                board[toRow][toCol] = target;
                                piece.setPosition(fromRow, fromCol);

                                if (isLegal) {
                                    return true;
                                }
                            }
                        }
                    }

                    if (piece instanceof King && !((King)piece).hasMoved()) {
                        if (isValidCastling(color, fromRow, fromCol, fromCol + 2)) {
                            return true;
                        }
                        if (isValidCastling(color, fromRow, fromCol, fromCol - 2)) {
                            return true;
                        }
                    }

                    if (piece instanceof Pawn) {
                        int direction = color.equals("white") ? -1 : 1;
                        int enPassantRow = color.equals("white") ? 3 : 4;

                        if (fromRow == enPassantRow) {
                            if (fromCol > 0 && board[fromRow][fromCol - 1] instanceof Pawn &&
                                    board[fromRow][fromCol - 1].getColor().equals(getOppositeColor(color)) &&
                                    enPassantTarget != null && enPassantTarget.equals(positionToNotation(fromRow, fromCol - 1))) {

                                Piece capturedPawn = board[fromRow][fromCol - 1];
                                board[fromRow + direction][fromCol - 1] = piece;
                                board[fromRow][fromCol] = null;
                                board[fromRow][fromCol - 1] = null;
                                piece.setPosition(fromRow + direction, fromCol - 1);

                                boolean isLegal = !isKingInCheck(color);

                                board[fromRow][fromCol] = piece;
                                board[fromRow][fromCol - 1] = capturedPawn;
                                board[fromRow + direction][fromCol - 1] = null;
                                piece.setPosition(fromRow, fromCol);

                                if (isLegal) {
                                    return true;
                                }
                            }

                            if (fromCol < 7 && board[fromRow][fromCol + 1] instanceof Pawn &&
                                    board[fromRow][fromCol + 1].getColor().equals(getOppositeColor(color)) &&
                                    enPassantTarget != null && enPassantTarget.equals(positionToNotation(fromRow, fromCol + 1))) {

                                Piece capturedPawn = board[fromRow][fromCol + 1];
                                board[fromRow + direction][fromCol + 1] = piece;
                                board[fromRow][fromCol] = null;
                                board[fromRow][fromCol + 1] = null;
                                piece.setPosition(fromRow + direction, fromCol + 1);

                                boolean isLegal = !isKingInCheck(color);

                                board[fromRow][fromCol] = piece;
                                board[fromRow][fromCol + 1] = capturedPawn;
                                board[fromRow + direction][fromCol + 1] = null;
                                piece.setPosition(fromRow, fromCol);

                                if (isLegal) {
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

    private String getOppositeColor(String color) {
        return color.equals("white") ? "black" : "white";
    }

    private boolean isInsufficientMaterial() {
        Map<String, Integer> pieceCount = new HashMap<>();
        pieceCount.put("white", 0);
        pieceCount.put("black", 0);

        boolean whiteHasBishopOrKnight = false;
        boolean blackHasBishopOrKnight = false;
        boolean whiteHasNonKing = false;
        boolean blackHasNonKing = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    String color = piece.getColor();
                    pieceCount.put(color, pieceCount.get(color) + 1);

                    if (!(piece instanceof King)) {
                        if (color.equals("white")) {
                            whiteHasNonKing = true;
                            if (piece instanceof Bishop || piece instanceof Knight) {
                                whiteHasBishopOrKnight = true;
                            }
                        } else {
                            blackHasNonKing = true;
                            if (piece instanceof Bishop || piece instanceof Knight) {
                                blackHasBishopOrKnight = true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public String toFEN() {
        StringBuilder fen = new StringBuilder();

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

        fen.append(" ").append(currentPlayer.equals("white") ? "w" : "b");

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

        fen.append(" ").append(enPassantTarget != null ? enPassantTarget : "-");

        fen.append(" ").append(halfMoveClock);

        fen.append(" ").append(fullMoveNumber);

        return fen.toString();
    }

    public boolean isResignation(String color) {
        return color.equals("white") ? whiteResigned : blackResigned;
    }

    public void setResignation(String color) {
        if (color.equals("white")) {
            whiteResigned = true;
        } else {
            blackResigned = true;
        }
    }

    public boolean isKingInCheck(String color) {
        King king = getKing(color);
        if (king == null) return false;

        String opponentColor = color.equals("white") ? "black" : "white";
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(row, col);
                if (piece != null && piece.getColor().equals(opponentColor)) {
                    if (piece.isValidMove(king.getRow(), king.getCol()) &&
                            isPathClear(row, col, king.getRow(), king.getCol())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isCheckmate(String color) {
        if (!isKingInCheck(color)) return false;
        return !hasLegalMoves(color);
    }

    public void setEnPassantTarget(String target) {
        this.enPassantTarget = target;
    }

    public boolean isValidCastling(String color, int row, int fromCol, int toCol) {
        int rookCol = toCol > fromCol ? 7 : 0;
        int step = toCol > fromCol ? 1 : -1;

        Piece king = getPieceAt(row, fromCol);
        Piece rook = getPieceAt(row, rookCol);

        if (!(king instanceof King) || !(rook instanceof Rook) ||
                ((King)king).hasMoved() || ((Rook)rook).hasMoved()) {
            return false;
        }

        for (int col = fromCol + step; col != rookCol; col += step) {
            if (getPieceAt(row, col) != null) {
                return false;
            }
        }

        String opponentColor = color.equals("white") ? "black" : "white";
        for (int col = fromCol; col != toCol + step; col += step) {
            if (isSquareUnderAttack(row, col, opponentColor)) {
                return false;
            }
        }

        return true;
    }
}