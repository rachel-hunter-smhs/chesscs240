package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
       ChessPiece piece = getBoard().getPiece(startPosition);
       if (piece == null) return null;
       if (piece.getTeamColor() != teamTurn) return List.of();
       List<ChessMove> legal = new ArrayList<>();
       for(ChessMove m: piece.pieceMoces(board, startPosition)){
           ChessBoard copy = duplicateBoard();
           applyMove(copy,m);
           if(!inCheck(copy, piece.getTeamColor())) legal.add(m);
       }
       return legal;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legal = validMoves(move.getStartPosition());
        if(legal == null || legal.stream().noneMatch(move::equals)) throw new InvalidMoveException();
        applyMove(board, move);
        teamTurn = teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return inCheck(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return inCheck(board, teamColor) && noLegalMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return  !inCheck(board, teamColor) && noLegalMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
       return board;
    }
    private ChessBoard duplicateBoard() {
        ChessBoard copy = new ChessBoard();
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p != null)
                    copy.addPiece(new ChessPosition(r, c),
                            new ChessPiece(p.getTeamColor(), p.getPieceType()));
            }
        return copy;
    }

    private void applyMove(ChessBoard b, ChessMove m) {
        ChessPiece moving = b.getPiece(m.getStartPosition());
        b.addPiece(m.getStartPosition(), null);
        ChessPiece placed = moving;
        if (m.getPromotionPiece() != null)
            placed = new ChessPiece(moving.getTeamColor(), m.getPromotionPiece());
        b.addPiece(m.getEndPosition(), placed);
    }

    private boolean inCheck(ChessBoard b, TeamColor side) {
        ChessPosition kingPos = null;
        outer:
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPiece p = b.getPiece(new ChessPosition(r, c));
                if (p != null && p.getTeamColor() == side &&
                        p.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = new ChessPosition(r, c);
                    break outer;
                }
            }
        if (kingPos == null) return true;
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPiece p = b.getPiece(new ChessPosition(r, c));
                if (p != null && p.getTeamColor() != side)
                    for (ChessMove m : p.pieceMoves(b, new ChessPosition(r, c)))
                        if (m.getEndPosition().equals(kingPos)) return true;
            }
        return false;
    }

    private boolean noLegalMoves(TeamColor side) {
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p != null && p.getTeamColor() == side)
                    if (!validMoves(new ChessPosition(r, c)).isEmpty()) return false;
            }
        return true;
    }
}
