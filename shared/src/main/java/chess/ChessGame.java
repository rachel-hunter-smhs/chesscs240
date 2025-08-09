// ChessGame.java
package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
        lastMove = null;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = getBoard().getPiece(startPosition);
        if (piece == null) return null;

        List<ChessMove> legal = new ArrayList<>();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            possibleMoves.addAll(getCastlingMoves(startPosition, piece.getTeamColor()));
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            possibleMoves.addAll(getEnPassantMoves(startPosition, piece.getTeamColor()));
        }

        for(ChessMove m: possibleMoves){
            ChessBoard copy = duplicateBoard();
            applyMove(copy, m);
            if(!inCheck(copy, piece.getTeamColor())) legal.add(m);
        }
        return legal;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.startPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> legal = validMoves(move.startPosition());
        if(legal == null || legal.stream().noneMatch(move::equals)) throw new InvalidMoveException();

        ChessPiece movingPiece = board.getPiece(move.startPosition());

        trackPieceMovement(move, movingPiece);

        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.endPosition().getColumn() - move.startPosition().getColumn()) == 2) {
            applyCastlingMove(move);
        }
        else if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                move.endPosition().getColumn() != move.startPosition().getColumn() &&
                board.getPiece(move.endPosition()) == null) {
            applyEnPassantMove(move);
        }
        else {
            applyMove(board, move);
        }

        lastMove = move;
        teamTurn = teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return inCheck(board, teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return inCheck(board, teamColor) && noLegalMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return  !inCheck(board, teamColor) && noLegalMoves(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteQueensideRookMoved = false;
        whiteKingsideRookMoved = false;
        blackQueensideRookMoved = false;
        blackKingsideRookMoved = false;
        lastMove = null;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private void trackPieceMovement(ChessMove move, ChessPiece piece) {
        ChessPosition start = move.startPosition();
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                if (start.equals(new ChessPosition(1, 1))) whiteQueensideRookMoved = true;
                else if (start.equals(new ChessPosition(1, 8))) whiteKingsideRookMoved = true;
            } else {
                if (start.equals(new ChessPosition(8, 1))) blackQueensideRookMoved = true;
                else if (start.equals(new ChessPosition(8, 8))) blackKingsideRookMoved = true;
            }
        }
    }

    private Collection<ChessMove> getCastlingMoves(ChessPosition kingPos, TeamColor color) {
        List<ChessMove> castlingMoves = new ArrayList<>();

        if (inCheck(board, color)) return castlingMoves;

        if (color == TeamColor.WHITE && !whiteKingMoved && kingPos.equals(new ChessPosition(1, 5))) {
            if (!whiteKingsideRookMoved && canCastle(kingPos, new ChessPosition(1, 8), color)) {
                castlingMoves.add(new ChessMove(kingPos, new ChessPosition(1, 7), null));
            }
            if (!whiteQueensideRookMoved && canCastle(kingPos, new ChessPosition(1, 1), color)) {
                castlingMoves.add(new ChessMove(kingPos, new ChessPosition(1, 3), null));
            }
        } else if (color == TeamColor.BLACK && !blackKingMoved && kingPos.equals(new ChessPosition(8, 5))) {
            if (!blackKingsideRookMoved && canCastle(kingPos, new ChessPosition(8, 8), color)) {
                castlingMoves.add(new ChessMove(kingPos, new ChessPosition(8, 7), null));
            }
            if (!blackQueensideRookMoved && canCastle(kingPos, new ChessPosition(8, 1), color)) {
                castlingMoves.add(new ChessMove(kingPos, new ChessPosition(8, 3), null));
            }
        }

        return castlingMoves;
    }

    private boolean canCastle(ChessPosition kingPos, ChessPosition rookPos, TeamColor color) {
        int startCol = Math.min(kingPos.getColumn(), rookPos.getColumn());
        int endCol = Math.max(kingPos.getColumn(), rookPos.getColumn());

        for (int col = startCol + 1; col < endCol; col++) {
            if (board.getPiece(new ChessPosition(kingPos.getRow(), col)) != null) {
                return false;
            }
        }

        int direction = rookPos.getColumn() > kingPos.getColumn() ? 1 : -1;
        for (int i = 1; i <= 2; i++) {
            int newCol = kingPos.getColumn() + i * direction;
            if (newCol < 1 || newCol > 8) break;
            ChessPosition testPos = new ChessPosition(kingPos.getRow(), newCol);
            ChessBoard testBoard = duplicateBoard();
            testBoard.addPiece(kingPos, null);
            testBoard.addPiece(testPos, new ChessPiece(color, ChessPiece.PieceType.KING));
            if (inCheck(testBoard, color)) {
                return false;
            }
        }

        return true;
    }

    private void applyCastlingMove(ChessMove move) {
        ChessPosition kingStart = move.startPosition();
        ChessPosition kingEnd = move.endPosition();
        boolean isKingside = kingEnd.getColumn() > kingStart.getColumn();

        ChessPiece king = board.getPiece(kingStart);
        board.addPiece(kingStart, null);
        board.addPiece(kingEnd, king);

        if (isKingside) {
            ChessPosition rookStart = new ChessPosition(kingStart.getRow(), 8);
            ChessPosition rookEnd = new ChessPosition(kingStart.getRow(), 6);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookStart, null);
            board.addPiece(rookEnd, rook);
        } else {
            ChessPosition rookStart = new ChessPosition(kingStart.getRow(), 1);
            ChessPosition rookEnd = new ChessPosition(kingStart.getRow(), 4);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookStart, null);
            board.addPiece(rookEnd, rook);
        }
    }

    private Collection<ChessMove> getEnPassantMoves(ChessPosition pawnPos, TeamColor color) {
        List<ChessMove> enPassantMoves = new ArrayList<>();

        if (lastMove == null) return enPassantMoves;

        ChessPiece lastMovedPiece = board.getPiece(lastMove.endPosition());
        if (lastMovedPiece == null || lastMovedPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return enPassantMoves;
        }

        int moveDistance = Math.abs(lastMove.endPosition().getRow() - lastMove.startPosition().getRow());
        if (moveDistance != 2) return enPassantMoves;

        if (Math.abs(pawnPos.getColumn() - lastMove.endPosition().getColumn()) != 1) {
            return enPassantMoves;
        }

        if (pawnPos.getRow() != lastMove.endPosition().getRow()) {
            return enPassantMoves;
        }

        int expectedRank = (color == TeamColor.WHITE) ? 5 : 4;
        if (pawnPos.getRow() != expectedRank) return enPassantMoves;

        int direction = (color == TeamColor.WHITE) ? 1 : -1;
        ChessPosition capturePos = new ChessPosition(pawnPos.getRow() + direction, lastMove.endPosition().getColumn());
        enPassantMoves.add(new ChessMove(pawnPos, capturePos, null));

        return enPassantMoves;
    }

    private void applyEnPassantMove(ChessMove move) {
        ChessPiece pawn = board.getPiece(move.startPosition());
        board.addPiece(move.startPosition(), null);
        board.addPiece(move.endPosition(), pawn);

        ChessPosition capturedPawnPos = new ChessPosition(move.startPosition().getRow(), move.endPosition().getColumn());
        board.addPiece(capturedPawnPos, null);
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
        ChessPiece moving = b.getPiece(m.startPosition());
        b.addPiece(m.startPosition(), null);
        ChessPiece placed = moving;
        if (m.promotionPiece() != null)
            placed = new ChessPiece(moving.getTeamColor(), m.promotionPiece());
        b.addPiece(m.endPosition(), placed);
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
                        if (m.endPosition().equals(kingPos)) return true;
            }
        return false;
    }

    private boolean noLegalMoves(TeamColor side) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p != null && p.getTeamColor() == side) {
                    if (!validMoves(new ChessPosition(r, c)).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame that)) return false;
        return Objects.equals(board, that.board) && teamTurn == that.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}