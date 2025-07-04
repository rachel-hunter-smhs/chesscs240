package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn() -1] =piece;
        
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
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
}
