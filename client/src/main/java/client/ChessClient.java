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
    private boolean evalPrelogin(String cmd, String[] tokens) throws  Exception{
        return switch (cmd){
            case "help" ->{
                printPreLoginHelp();
                yield false;
            }
            case "quit" -> true;
            case "register" -> {
                register(tokens);
                yield false;
            }
            case "login" -> {
                login(tokens);
                yield false;
            }
            default -> {
                System.out.println("Unknown command. Type 'help' for available commands");
                yield false;
            }


        };
    }
    private boolean evalPostlogin(String cmd, String[] tokens) throws  Exception{
        return switch (cmd){
            case "help" ->{
                printPostLoginHelp();
                yield false;
            }
            case "logout" -> {
                logout();
                yield false;
            }
            case "create" -> {
                createGame(tokens);
                yield false;
            }
            case "list" -> {
                listGames();
                yield false;
            }
            case "play" ->{
                playGame(tokens);
                yield false;
            }

            default -> {
                System.out.println("Unknown command. Type 'help' for available commands");
                yield false;
            }


        };
    }
    private void printPreLoginHelp(){
        System.out.println("Possible commands");
        System.out.println(" register <username> <password> <email> : Creates new account");
        System.out.println(" login <username> <password> : logins into to your account");
        System.out.println(" quit : exits program");
        System.out.println(" help: shows help message");
    }
    private void printPostLoginHelp(){
        System.out.println("Possible commands");
        System.out.println(" create <game name> : Creates new game");
        System.out.println(" list : lists all games");
        System.out.println(" play <game number> <WHITE|BLACK> : Joins game");
        System.out.println(" observe <game number>: Observes game");
        System.out.println(" logout : logs out");
        System.out.println(" help: prints this message");
    }

}
