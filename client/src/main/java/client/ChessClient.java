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
    public void run(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'help' to start");
        while (true){
            printPrompt();
            String line = scanner.nextLine().trim();

            if (line.isEmpty()){
                continue;
            }
            try {
                if (eval(line)){
                    break;
                }
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }
    private void printPrompt(){
        if(state == State.PRELOGIN){
            System.out.print("[LOGGED OUT] >>>");
        } else{
            System.out.print("[LOGGED IN] >>>");
        }
    }
    private boolean eval(String input) throws Exception{
        String[] tokens = input.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        if(state == State.PRELOGIN {
            return evalPrelogin(cmd, tokens);
        } else{
            return evalPostlogin(cmd, tokens);
        }
    }

}
