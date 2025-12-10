package websocket.commands;

public class GameCommandUser {
    public  enum  CommandType{
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }
    protected CommandType commandType;
    protected String authToken;
    protected Integer gameID;
    public GameCommandUser(CommandType commandType, String authToken, Integer gameID){
        this.authToken = authToken;
        this.commandType = commandType;
        this.gameID = gameID;

    }

    public CommandType getCommandType() {
        return commandType;
    }
    public String getAuthToken(){
        return authToken;
    }
    public Integer getGameID(){
        return gameID;
    }
}
