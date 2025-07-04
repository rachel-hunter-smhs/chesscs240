package chess;

import java.util.Collection;
import java.util.ArrayList;
/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition from myPosition;

        switch (pieceType){
            case PAWN -> {
                int dir = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
                int startRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
                int nextRow = row + dir;

                if(inBounds(nextRow, col) && board.getPiece(new ChessPosition(nextRow, col)) == null){
                    addPawnAdvance(moves, from new ChessPosition(nextRow, col));
                    if (row == startRow){
                        int jumpRow = row + 2 * dir;
                        if(board.getPiece(new ChessPosition(jumpRow, col)) == null)
                            moves.add(new ChessMove(from, new ChessPosition(jumpRow, col), null));
                    }
                }

                for(int dc : new int[]{-1,1}){
                    int captureCol = col + dc;
                    if (inBounds(nextRow, captureCol)){
                        ChessPiece target = board.getPiece(new ChessPosition(nextRow, captureCol));
                        if (target != null && target.teamColor != teamColor)
                            addPawnAdvance(moves, from, new ChessPosition(nextRow, captureCol));
                    }
                }
            }
            case KNIGHT -> {
                int[][] d = {{2,1}, {1,2}, {-1,2}, {-2,1}, {-2, -1}, {-1,-2}, {1,-2}, {2,-1}};
                for (int[] s : d){
                    int r = row + s[0], c = col + s[1];
                    if(inBounds(r,c)){
                        ChessPiece tgt = board.getPiece(new ChessPosition(r,c), null);
                    }
                }
            }
            case KING -> {
                for (int dr = -1; dr <= 1; dr ++)
                    for(int dc = -1; dc <= 1; dc++)
                        if(dr != 0 || dc !=0){
                            int r = row + dr, c = col + dc;
                            if (inBounds(r,c)){
                                ChessPiece tgt = board.getPiece(new ChessPosition(r,c));
                                if (tgt == null || tgt.teamColor != teamColor)
                                    moves.add(new ChessMove(from, new ChessPosition(r,c), null));
                            }
                        }
            }
            case BISHOP -> slide(board, from, moves, new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}});
            case ROOK -> slide(board, from, moves, new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}});
            case QUEEN -> slide(board, from, moves, new int[][]{
                    {1,0}, {-1,0}, {0,1}, {0,-1},
                    {1,1}, {1,-1}, {-1,1}, {-1,-1}});

        }
        return moves;
    }
    private void addPawnAdvance(Collection<ChessMove> moves, ChessPosition from, ChessPosition to){
        int promoRow = ( teamColor == ChessGame.TeamColor.WHITE) ? 8 : 1;
        if (to.getRow() == promoRow){
            moves.add(new ChessMove(from, to, PieceType.QUEEN));
            moves.add(new ChessMove(from, to, PieceType.ROOK));
            moves.add(new ChessMove(from, to, PieceType.BISHOP));
            moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

}
