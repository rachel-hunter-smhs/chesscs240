package chess;

import java.util.Collection;
import java.util.ArrayList;

public class ChessPiece {
    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        switch (pieceType) {
            case PAWN -> {
                int dir = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
                int startRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
                int nextRow = row + dir;

                if (inBounds(nextRow, col) && board.getPiece(new ChessPosition(nextRow, col)) == null) {
                    addPawnAdvance(moves, myPosition, new ChessPosition(nextRow, col));
                    if (row == startRow) {
                        int jumpRow = row + 2 * dir;
                        if (board.getPiece(new ChessPosition(jumpRow, col)) == null)
                            moves.add(new ChessMove(myPosition, new ChessPosition(jumpRow, col), null));
                    }
                }

                for (int dc : new int[]{-1, 1}) {
                    int captureCol = col + dc;
                    if (inBounds(nextRow, captureCol)) {
                        ChessPiece target = board.getPiece(new ChessPosition(nextRow, captureCol));
                        if (target != null && target.teamColor != teamColor)
                            addPawnAdvance(moves, myPosition, new ChessPosition(nextRow, captureCol));
                    }
                }
            }
            case KNIGHT -> {
                int[][] d = {{ 2, 1}, { 1, 2}, {-1, 2}, {-2, 1},
                        {-2,-1}, {-1,-2}, { 1,-2}, { 2,-1}};
                for (int[] s : d) {
                    int r = row + s[0], c = col + s[1];
                    if (inBounds(r, c)) {
                        ChessPiece tgt = board.getPiece(new ChessPosition(r, c));
                        if (tgt == null || tgt.getTeamColor() != teamColor)
                            moves.add(new ChessMove(myPosition, new ChessPosition(r, c), null));
                    }
                }
            }

            case KING -> {
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++)
                        if (dr != 0 || dc != 0) {
                            int r = row + dr, c = col + dc;
                            if (inBounds(r, c)) {
                                ChessPiece tgt = board.getPiece(new ChessPosition(r, c));
                                if (tgt == null || tgt.teamColor != teamColor)
                                    moves.add(new ChessMove(myPosition, new ChessPosition(r, c), null));
                            }
                        }
            }
            case BISHOP -> slide(board, myPosition, moves, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
            case ROOK -> slide(board, myPosition, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
            case QUEEN -> slide(board, myPosition, moves, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}});

        }
        return moves;
    }

    private void addPawnAdvance(Collection<ChessMove> moves, ChessPosition myPosition, ChessPosition to) {
        int promoRow = (teamColor == ChessGame.TeamColor.WHITE) ? 8 : 1;
        if (to.getRow() == promoRow) {
            moves.add(new ChessMove(myPosition, to, PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, to, PieceType.ROOK));
            moves.add(new ChessMove(myPosition, to, PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, to, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(myPosition, to, null));
        }
    }

    private void slide(ChessBoard board, ChessPosition from, Collection<ChessMove> moves, int[][] dirs) {
        int row = from.getRow(), col = from.getColumn();
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            while (inBounds(r, c)) {
                ChessPosition to = new ChessPosition(r, c);
                ChessPiece tgt = board.getPiece(to);
                if (tgt == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (tgt.teamColor != teamColor)
                        moves.add(new ChessMove(from, to, null));
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return 31 * teamColor.hashCode() + pieceType.hashCode();
    }
}