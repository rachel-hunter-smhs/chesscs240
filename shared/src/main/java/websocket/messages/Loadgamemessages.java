package websocket.messages;
import chess.ChessGame;

public class Loadgamemessages extends ServerMessage{
    private final ChessGame game;
    public Loadgamemessages(ChessGame game){
        super (ServerMessageType.LOAD_GAME);
        this.game = game;
    }
    public ChessGame getGame(){
        return game;

    }
}
