package chess;

import java.util.Arrays;

public class ChessBoard {
    private final ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn() -1] = piece;

    }

    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    public void resetBoard() {
        for (ChessPiece[] row : squares) Arrays.fill(row, null);

        var white = ChessGame.TeamColor.WHITE;
        var black = ChessGame.TeamColor.BLACK;

        ChessPiece.PieceType[] back = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK,
        };
        for (int c = 1; c<=8; c++ ){
            addPiece(new ChessPosition(2,c), new ChessPiece(white, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7,c), new ChessPiece(black, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(1,c), new ChessPiece(white, back[c-1]));
            addPiece(new ChessPosition(8,c), new ChessPiece(black, back[c-1]));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessBoard)) return false;
        return java.util.Arrays.deepEquals(squares, ((ChessBoard) o).squares);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(squares);
    }
}