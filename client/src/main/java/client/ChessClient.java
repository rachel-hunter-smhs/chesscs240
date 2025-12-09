package client;
import java.util.Scanner;

public class ChessClient {
    private  final ServerFacade server;
    private String authToken = null;
    private  State state = State.PRELOGIN;

    private enum State{
        PRELOGIN,
        POSTLOGIN
    }
    public ChessClient(ServerFacade server){
        this.server = server;
    }
}
