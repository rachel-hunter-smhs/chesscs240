import client.ServerFacade;
import client.ChessClient;
public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if(args.length == 1){
            try{
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port num is Invalid. Using default port 8080");
            }
        }
        System.out.println("♕ Welcome to 240 Chess Client ♕");
        System.out.println("Connecting to server at http://localhost:" + port);

        ServerFacade server = new ServerFacade(port);
        ChessClient client = new ChessClient(server);
        client.run();

    }

}