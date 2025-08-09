package chess;

import java.util.Objects;
import chess.ChessPiece.PieceType;

public record ChessMove(ChessPosition startPosition, ChessPosition endPosition, PieceType promotionPiece) {
    public ChessPosition getStartPosition() { return startPosition; }
    public ChessPosition getEndPosition() { return endPosition; }
    public PieceType getPromotionPiece() { return promotionPiece; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessMove that)) return false;
        return Objects.equals(startPosition, that.startPosition)
                && Objects.equals(endPosition, that.endPosition)
                && promotionPiece == that.promotionPiece;
    }

    @Override
    public String toString() {
        return "ChessMove{start=" + startPosition + ", end=" + endPosition + ", promotion" + promotionPiece + "}";
    }
}
