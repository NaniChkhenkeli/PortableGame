package com.portablegame.main.model;

public class ChessModel {

    public enum Color {
        WHITE,
        BLACK;

        public Color opposite() {
            return this == WHITE ? BLACK : WHITE;
        }
    }

    public enum PieceType{
        PAWN,
        KNIGHT,
        BISHOP,
        ROOK,
        QUEEN,
        KING;
    }

    public enum GameState {
        ONGOING,
        WHITE_WIN,
        BLACK_WIN,
        DRAW,
        CHECK,
        STALEMATE;

        public boolean isTerminal() {
            return this != ONGOING && this != CHECK;
        }
    }

    public static class Piece {
        private final PieceType type;
        private final Color color;

        public Piece(PieceType type, Color color) {
            this.type = type;
            this.color = color;
        }

        public PieceType getType() {return type; }
        public Color getColor(){return color; }

        @Override
        public String toString() {
            return color.name().charAt(0) + type.name().substring(0,2);
        }
    }

    public static class Square {
        private final int file;  // 0-7 (a-h)
        private final int rank; // 0-7 (1-8)

        public Square(int file, int rank) {
            if (file < 0 || file > 7 || rank < 0 || rank > 7)
                throw new IllegalArgumentException("invalid dquare coordinates");

            this.file = file;
            this.rank = rank;
        }

        public static Square fromString(String notation) {
            if (notation.length() != 2) throw new IllegalArgumentException();
            int file = notation.charAt(0) - 'a';
            int rank = notation.charAt(1) - '1';
            return new Square(file, rank);
        }

        public int getFile() {return file;}
        public int getRank() {return rank;}

        @Override
        public String toString() {
            return "" + (char)('a' + file) + (rank + 1);
        }
    }
}
