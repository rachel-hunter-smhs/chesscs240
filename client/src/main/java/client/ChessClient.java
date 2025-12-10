package client;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;
    private ServerFacade.GameData[] gameList = null;
    private State state = State.PRELOGIN;

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
                e.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }

    private void printPrompt(){
        if(state == State.PRELOGIN){
            System.out.print("[LOGGED OUT] >>> ");
        } else{
            System.out.print("[LOGGED IN] >>> ");
        }
    }

    private boolean eval(String input) throws Exception{
        String[] tokens = input.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        if(state == State.PRELOGIN) {
            return evalPrelogin(cmd, tokens);
        } else{
            return evalPostlogin(cmd, tokens);
        }
    }

    private boolean evalPrelogin(String cmd, String[] tokens) throws Exception{
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

    private boolean evalPostlogin(String cmd, String[] tokens) throws Exception{
        return switch (cmd){
            case "help" -> {
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
            case "observe" ->{
                observeGame(tokens);
                yield false;
            }
            default -> {
                System.out.println("Unknown command. Type 'help' for available commands");
                yield true;
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

    private void register(String[] tokens) throws Exception{
        if (tokens.length != 4){
            System.out.println(" register needs: register <username> <password> <email>");
            return;
        }
        var authData = server.register(tokens[1], tokens[2], tokens[3]);
        authToken = authData.authToken();
        state = State.POSTLOGIN;
        System.out.println("Successfully registered and logged in as " + authData.username());
    }

    private void login(String[] tokens) throws Exception{
        if (tokens.length != 3){
            System.out.println("Login requires: login <username> <password>");
            return;
        }
        var authData = server.login(tokens[1], tokens[2]);
        authToken = authData.authToken();
        state = State.POSTLOGIN;
        System.out.println("Successfull logged in as " + authData.username());
    }

    private void logout() throws Exception{
        server.logout(authToken);
        authToken = null;
        state = State.PRELOGIN;
        System.out.println("Successfully logged out");
    }

    private void createGame(String[] tokens) throws Exception{
        if (tokens.length < 2){
            System.out.println("create needs : create <game name>");
            return;
        }

        // FIX: Build game name from all tokens after "create"
        StringBuilder gameName = new StringBuilder();
        for (int i = 1; i < tokens.length; i++){
            if (i > 1) {
                gameName.append(" ");
            }
            gameName.append(tokens[i]);
        }

        var response = server.createGame(authToken, gameName.toString());
        System.out.println("Created Game " + gameName + " (ID" + response.gameID() + ")");
    }

    private void listGames() throws Exception{
        var result = server.listGames(authToken);
        gameList = result.games();
        if (result.games().length == 0){
            System.out.println("No available games");
            return;
        }
        System.out.println("Available games: ");
        for (int i = 0; i< result.games().length; i++){
            var game = result.games()[i];
            String white = game.whiteUsername() != null ? game.whiteUsername() : "available";
            String black = game.blackUsername() != null ? game.blackUsername() : "available";
            System.out.println((i + 1) + ". " + game.gameName() + " White: " + white + ", Black: " + black);
        }
    }

    private void playGame(String[] tokens) throws Exception{
        System.out.println("Play game");
    }

    private void observeGame(String[] tokens) throws Exception{
        System.out.println("Observe game");
    }
}