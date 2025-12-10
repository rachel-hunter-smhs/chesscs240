package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private final String errorsMessage;
    public ErrorMessage(String errorsMessage){
        super(ServerMessageType.ERROR);
        this.errorsMessage = errorsMessage;
    }
    public String getErrorMessage (){
        return errorsMessage;
    }

}
