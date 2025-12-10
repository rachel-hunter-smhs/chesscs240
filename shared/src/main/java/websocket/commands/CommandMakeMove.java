package websocket.commands;
import chess.ChessMove;

public class CommandMakeMove extends GameCommandUser {
    private  ChessMove chessMove;
    public CommandMakeMove(String authToken, Integer gameID, ChessMove chessMove){
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.chessMove =chessMove;
    }
    public ChessMove getChessMove(){
        return chessMove;
    }

}
