package websocket.message;

public class ErrorMessage extends ServerMessage{
    private final String errorsIssue;
    public ErrorMessage(String errorsIssue){
        super(ServerMessageType.ERROR);
        this.errorsIssue = errorsIssue;
    }
    public String getErrorMessage (){
        return errorsIssue;
    }

}
