package client;


import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.Loadgamemessages;
import websocket.messages.NotificationMessage;

import java.net.URI;
@ClientEndpoint
public class WebSocketFacade {
    private Session sesh;
    private final Gson gson = new Gson();
    private ServerMessageType fixer;
    public WebSocketFacade(String url, ServerMessageType fixer) throws Exception{
        URI uri = new URI(url);
        this.fixer = fixer;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.sesh = container.connectToServer(this, uri);
    }


    @OnOpen
    public void  onOpen(Session session){
        System.out.println("WebSocket connected to server");
    }
    @OnMessage
    public void onMessage(String message, Session session) {
        com.google.gson.JsonObject jsonObject = gson.fromJson(message, com.google.gson.JsonObject.class);
        String messageType = jsonObject.get("serverMessageType").getAsString();
       try {
           switch (messageType){
               case "LOAD_GAME" -> {
                   Loadgamemessages load = gson.fromJson(message, Loadgamemessages.class);
                   fixer.LOAD_GAME(load.getGame());
               }
               case "NOTIFICATION" -> {
                   NotificationMessage note = gson.fromJson(message, NotificationMessage.class);
                   fixer.NOTIFICATION(note.getNotification());
               }
               case "ERROR" -> {
                   ErrorMessage err = gson.fromJson(message, ErrorMessage.class);
                   fixer.ERROR(err.getErrorMessage());
               }
           }

       } catch (Exception e) {
           System.err.println("Error parsing message: " + e.getMessage());

       }
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
    public void connect(String authToken, int gameid) throws Exception{
        UserGameCommand command = new UserGameCommand( UserGameCommand.CommandType.CONNECT,  authToken, gameid);
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
