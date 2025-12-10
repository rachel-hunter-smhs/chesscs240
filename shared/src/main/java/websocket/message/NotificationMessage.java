package websocket.message;

public class NotificationMessage extends ServerMessage {
    private final String notification;



    public NotificationMessage(String notification) {
        super(ServerMessage.ServerMessageType.NOTIFICATION);
        this.notification = notification;
    }


    public String getNotification() {
        return notification;
    }
}
