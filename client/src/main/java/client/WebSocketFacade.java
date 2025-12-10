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
}
