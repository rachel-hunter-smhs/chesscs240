package server.websocket;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebsocketHandler {
    private final   Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception{
        System.out.println("Websocket connection: " + session.getRemoteAddress()) ;
    }
    @OnWebSocketClose
    public void onClose
}
