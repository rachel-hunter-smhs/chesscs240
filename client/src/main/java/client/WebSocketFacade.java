package client;

import com.google.gson.Gson;
import websocket.commands.GameCommandUser;
import websocket.message.ServerMessage;
import javax.websocket.*;
import java.net.URI;
@ClientEndpoint
public class WebSocketFacade {
    private Session sesh;
    private final Gson gson = new Gson();
    public WebSocketFacade(String url) throws Exception{
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.sesh = container.connectToServer(this, uri);
    }
    @OnOpen
    public void  onOpen(Session session){
        System.out.println("WebSocket connected to server");
    }
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received from server: " + message);
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
    public void connect(String authToken, int gameid) throws Exception{
        GameCommandUser command = new GameCommandUser( GameCommandUser.CommandType.CONNECT,  authToken, gameid);
        String json = gson.toJson(command);
        System.out.println("Sending connect: " + json);
        send(json);
    }

    public void send(String message) throws Exception {
        sesh.getBasicRemote().sendText(message);
    }

    public void close() throws Exception {
        sesh.close();
    }
}
