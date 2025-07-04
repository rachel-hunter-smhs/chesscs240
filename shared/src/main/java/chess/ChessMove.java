package chess;

import java.util.Objects;
import chess.ChessPiece.PieceType;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }
    @Override
    public boolean equals (Object o){
        if (this == o) return true;
        if (!(o instanceof ChessMove)) return false;
        ChessMove that = (ChessMove) o;
        return Objects.equals(startPosition, that.startPosition)
                && Objects.equals(endPosition, that.endPosition)
                && promotionPiece == that.promotionPiece;

    }
    @Override
    public int hashCode(){
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }
    @Override
    public String toString() {
        return "ChessMove{" +
                "start=" + startPosition +
                ", end=" + endPosition +
                ", promotion" + promotionPiece +
                '}';
    }

}
