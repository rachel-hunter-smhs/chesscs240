package client;
import chess.*;
import ui.EscapeSequences;
import static  ui.EscapeSequences.*;
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
        for(int r = 8; r >=1; r--){
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
        System.out.print(BORDER_COLOR + "   ");
        if(whiteView){
            for(char c = 'a'; c <= 'h'; c++){
                System.out.print(" " + c + " ");
            }
        } else{
            for(char c = 'h'; c >= 'a'; c--){
                System.out.print(" " + c + " ");
            }
        }
        System.out.print("   "+ RESET_BG_COLOR);
        System.out.println();

    }
    private static void drawRow(ChessBoard board, int r, boolean whiteView){
        System.out.print(BORDER_COLOR + " " + r + " " + RESET_BG_COLOR);
        if (whiteView){
            for(int c = 1; c <=8; c++){
                drawSquare(board, r, c);
            }
        } else{
            for(int c = 8; c <=1; c++){
                drawSquare(board, r, c);
            }
        }
    }
}
