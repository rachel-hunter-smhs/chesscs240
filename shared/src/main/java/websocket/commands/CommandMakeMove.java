package websocket.commands;
import chess.ChessMove;

public class CommandMakeMove extends UserGameCommand {
    private final ChessMove move;
    public CommandMakeMove(String authToken, Integer gameID, ChessMove move){
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }
    public ChessMove getChessMove(){
        return move;
    }

}
