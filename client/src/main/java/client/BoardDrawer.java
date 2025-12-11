package client;
import chess.*;
import static ui.EscapeSequences.*;

public class BoardDrawer {
    private static final String LIGHT_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_COLOR = SET_BG_COLOR_DARK_GREEN;

    public static void drawBoard(ChessGame game, ChessGame.TeamColor view){
        ChessBoard board = game.getBoard();
        if(view == ChessGame.TeamColor.WHITE){
            drawWhiteView(board);
        }
        else{
            drawBlackView(board);
        }
    }

    private static void drawWhiteView(ChessBoard board){
        System.out.println();
        drawBorderRow(true);
        for(int r = 8; r >= 1; r--){
            drawRow(board, r, true);
        }
        drawBorderRow(true);
        System.out.println();
    }

    private static void drawBlackView(ChessBoard board){
        System.out.println();
        drawBorderRow(false);
        for(int r = 1; r <= 8; r++){
            drawRow(board, r, false);
        }
        drawBorderRow(false);
        System.out.println();
    }

    private static void drawBorderRow(boolean whiteView){
        System.out.print(BORDER_COLOR + EMPTY);
        if(whiteView){
            for(char c = 'a'; c <= 'h'; c++){
                System.out.print(" " + c + " ");
            }
        } else{
            for(char c = 'h'; c >= 'a'; c--){
                System.out.print(" " + c + " ");
            }
        }
        System.out.print(EMPTY + RESET_BG_COLOR);
        System.out.println();
    }

    private static void drawRow(ChessBoard board, int r, boolean whiteView){
        System.out.print(BORDER_COLOR + " " + r + " " + RESET_BG_COLOR);
        if (whiteView){
            for(int c = 1; c <= 8; c++){
                drawSquare(board, r, c);
            }
        } else{
            for(int c = 8; c >= 1; c--){
                drawSquare(board, r, c);
            }
        }
        System.out.print(BORDER_COLOR + " " + r + " " + RESET_BG_COLOR);
        System.out.println();
    }

    private static void drawSquare(ChessBoard board, int r, int c){
        boolean lightSquare = (r + c) % 2 != 0;
        String bgColor = lightSquare ? LIGHT_SQUARE : DARK_SQUARE;

        ChessPosition pos = new ChessPosition(r, c);
        ChessPiece piece = board.getPiece(pos);

        String cellText;
        String textColor = "";

        if(piece == null){
            cellText = "   ";
        } else{
            String sym = pieceLetter(piece);
            cellText = " " + sym + " ";
            boolean colorWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
            textColor = colorWhite ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        }

        if(piece == null){
            System.out.print(bgColor + cellText + RESET_BG_COLOR);
        } else{
            System.out.print(bgColor + textColor + cellText + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
    }

    private static String pieceLetter(ChessPiece p){
        return switch (p.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }

    public static void main(String[] args) {
        ChessGame game = new ChessGame();
        game.getBoard().resetBoard();

        System.out.println("WHITE VIEW:");
        drawBoard(game, ChessGame.TeamColor.WHITE);

        System.out.println("\n\nBLACK VIEW:");
        drawBoard(game, ChessGame.TeamColor.BLACK);
    }
}
