package server.websocket;
import  org.eclipse.jetty.websocket.api.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class Connections {
    private final ConcurrentHashMap < Integer, List<Session>> connect = new ConcurrentHashMap<>();

    public void add(int gameID, Session sesh){
        connect.computeIfAbsent(gameID, k -> new ArrayList<>()).add(sesh);
    }
    public void remove(int gameID, Session sesh){
        var seshs = connect.get(gameID);
        if(seshs != null){
            seshs.remove(sesh);
            if(seshs.isEmpty()){
                connect.remove(gameID);
            }
        }
    }
    public void broadcast(int gameId, String message, Session excludeSesh) throws Exception{
        var seshs = connect.get(gameId);
        if(seshs != null ){
            for (Session sesh : new ArrayList<>(seshs)){
                if(sesh.isOpen() && !sesh.equals(excludeSesh)){
                    sesh.getRemote().sendString(message);
                }
            }
        }
    }
    public void sendSesh(Session sesh, String val) throws Exception{
        if(sesh.isOpen()){
            sesh.getRemote().sendString(val);
        }
    }
}
