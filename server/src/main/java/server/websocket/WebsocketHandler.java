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
    public void onClose(Session session, int codeStat, String reasoning){
        System.out.println("Websocket closed" + session.getRemoteAddress());
    }
    @OnWebSocketMessage
    public void onMessage(Session session, String stat){
        System.out.println("Recieved Message: " + stat);
        //session.getRemote().sendString("Echo: " + stat);

    }
}
