package client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final String serverLink;
    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public ServerFacade(int port) {
        this.serverLink = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
    }
    public AuthData register(String username, String password, String email) throws Exception{
        var request= new RegistrationRequest(username,password,email);
        return sendRequest("POST", "/user", request, AuthData.class, null);
    }
    private <T> T sendRequest(String method,
                              String path,
                              Object requestObject,
                              Class<T> responseClass,
                              String authToken) throws Exception {

        String url = serverLink + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        if (authToken != null && !authToken.isEmpty()) {
            builder.header("authorization", authToken);
        }
        String requestBody = requestObject == null ? "" : gson.toJson(requestObject);
        builder.header("Content-Type", "application/json");

        switch (method) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(requestBody));
            case "DELETE" -> {
                if (requestObject == null) {
                    builder.DELETE();
                } else {
                    builder.method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody));
                }
            }
            default -> throw new IllegalArgumentException("Unsupported method " + method);
        }

        HttpRequest httpRequest = builder.build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String body = response.body();

        if (status / 100 != 2) {
            if (body != null && !body.isEmpty()) {
                try {
                    ErrorResponse error = gson.fromJson(body, ErrorResponse.class);
                    if (error != null && error.message != null && !error.message.isEmpty()) {
                        throw new Exception(error.message);
                    }
                } catch (Exception ignored) {
                }
            }
            throw new Exception("Request failed with status " + status);
        }

        if (responseClass == null) {
            return null;
        }

        if (body == null || body.isEmpty()) {
            return null;
        }

        return gson.fromJson(body, responseClass);
    }
    public AuthData login(String username, String password) throws Exception{
        var request = new LoginRequest(username, password);
        return sendRequest("POST", "/session",request, AuthData.class,null);
    }
    public void logout(String authToken) throws Exception {
        sendRequest("DELETE", "/session", null, null, authToken);
    }
    public GameListResult listGames(String authToken) throws Exception {
        var result = sendRequest("GET", "/game", null, GameListResult.class, authToken);
        assert result != null;
        return new GameListResult(result.games() != null ? result.games() : new GameData[0]);
    }
    public CreateGameResponse createGame (String AuthToken, String gameName) throws Exception{
        var request = new CreateGameRequest(gameName);
        return sendRequest("POST", "/game", request, CreateGameResponse.class, AuthToken);


    }

    public record RegistrationRequest(String username, String password, String email) {}

    public record LoginRequest(String username, String password) {}

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var req = new JoinGameRequest(playerColor, gameID);
        sendRequest("PUT", "/game", req, null, authToken);
    }


    public record CreateGameRequest(String gameName) {}

    public record CreateGameResponse(int gameID){}

    public record JoinGameRequest(String playerColor, int gameID) {}
    public record ObserveGameRequest(int gameID){}

    public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    public record GameListResult(GameData[] games) {}
    public static class ErrorResponse {
        public String message;
    }
    public void clear() throws Exception {
        sendRequest("DELETE", "/db", null, null, null);
    }


}
